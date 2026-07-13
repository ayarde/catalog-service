package com.ecommerce.catalog.infrastructure.adapter.in.web.controller;

import com.ecommerce.catalog.application.dto.ActivateProductCommand;
import com.ecommerce.catalog.application.dto.CreateProductCommand;
import com.ecommerce.catalog.application.dto.DeleteProductCommand;
import com.ecommerce.catalog.application.dto.UpdateProductCommand;
import com.ecommerce.catalog.application.port.in.*;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.infrastructure.adapter.in.web.config.OpenApiConfig;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.PagedProductResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.PagedResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ProductCreateRequest;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ProductResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ValidationErrorResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.VariantAvailabilityResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.mapper.ProductWebMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Adaptador de Entrada (API REST).
 * Expone los puertos (Casos de Uso) del Dominio hacia el exterior (Next.js, APPs).
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Catalog Service", description = "Operaciones principales de lectura y escritura del catálogo de productos")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ActivateProductUseCase activateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final GetProductBySlugUseCase getProductBySlugUseCase;
    private final UpdateStockUseCase updateStockUseCase;
    private final CheckVariantAvailabilityUseCase checkVariantAvailabilityUseCase;
    private final ProductWebMapper mapper;

    public ProductController(
            CreateProductUseCase createProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            ActivateProductUseCase activateProductUseCase,
            DeleteProductUseCase deleteProductUseCase,
            GetProductUseCase getProductUseCase,
            ListProductsUseCase listProductsUseCase,
            GetProductBySlugUseCase getProductBySlugUseCase,
            UpdateStockUseCase updateStockUseCase,
            CheckVariantAvailabilityUseCase checkVariantAvailabilityUseCase,
            ProductWebMapper mapper) {
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.activateProductUseCase = activateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.getProductBySlugUseCase = getProductBySlugUseCase;
        this.updateStockUseCase = updateStockUseCase;
        this.checkVariantAvailabilityUseCase = checkVariantAvailabilityUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(
            operationId = "createProduct",
            summary = "Crear producto",
            description = "Da de alta un nuevo producto en el catálogo en estado DRAFT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "SKU duplicado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ProductResponse> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del producto a crear", required = true)
            @Valid @RequestBody ProductCreateRequest request) {
        log.info("REST Request to create Product with SKU: {}", request.skuBase());

        CreateProductCommand command = mapper.toCommand(request);
        Product product = createProductUseCase.create(command);
        ProductResponse response = mapper.toResponse(product);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "getProductById",
            summary = "Obtener producto por ID",
            description = "Retorna los detalles de un producto usando su ID numérico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content)
    })
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id) {
        log.info("REST Request to get Product : {}", id);
        return getProductUseCase.getById(id)
                .map(product -> ResponseEntity.ok().body(mapper.toResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @RateLimiter(name = "catalogList", fallbackMethod = "listProductsFallback")
    @Operation(
            operationId = "listProducts",
            summary = "Listar productos",
            description = "Retorna el listado paginado de productos del catálogo. "
                    + "El parámetro size se limita internamente a un máximo de 100."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado paginado",
                    content = @Content(schema = @Schema(implementation = PagedProductResponse.class))),
            @ApiResponse(responseCode = "429", description = "Límite de peticiones excedido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<ProductResponse> listProducts(
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST Request to list Products page: {}, size: {}", page, size);
        int limitSize = Math.min(size, 100);
        return mapper.toPagedResponse(listProductsUseCase.list(page, limitSize));
    }

    public PagedResponse<ProductResponse> listProductsFallback(int page, int size, Throwable t) {
        log.warn("Fallback triggered for listProducts(page={}, size={}) due to {}: {}", page, size, t.getClass().getSimpleName(), t.getMessage());
        return new PagedResponse<>(List.of(), page, size, 0L, 0, true);
    }

    @PutMapping("/{id}/activate")
    @Operation(
            operationId = "activateProduct",
            summary = "Activar producto",
            description = "Cambia el estado de un producto a ACTIVE para que sea visible en el storefront."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto activado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ProductResponse> activateProduct(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id) {
        log.info("REST Request to activate Product : {}", id);
        Product product = activateProductUseCase.activate(new ActivateProductCommand(id));
        return ResponseEntity.ok(mapper.toResponse(product));
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "updateProduct",
            summary = "Actualizar detalles del producto",
            description = "Actualiza la información base de un producto (nombre, descripción, precio base, variantes e imágenes)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del producto", required = true)
            @Valid @RequestBody ProductCreateRequest request) {
        log.info("REST Request to update Product : {}", id);
        CreateProductCommand command = mapper.toCommand(request);
        Product product = updateProductUseCase.update(new UpdateProductCommand(
                id,
                command.name(),
                command.description(),
                command.basePrice(),
                command.currency(),
                command.variants(),
                command.images()
        ));
        return ResponseEntity.ok(mapper.toResponse(product));
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "archiveProduct",
            summary = "Archivar producto",
            description = "Cambia el estado de un producto a ARCHIVED (soft delete)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto archivado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id) {
        log.info("REST Request to delete Product : {}", id);
        deleteProductUseCase.delete(new DeleteProductCommand(id, "REST API Request"));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/variants/{variantId}/stock")
    @Operation(
            operationId = "updateVariantStock",
            summary = "Actualizar stock de variante",
            description = "Establece la cantidad de stock de una variante concreta de un producto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock actualizado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto o variante no encontrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ProductResponse> updateStock(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID de la variante", example = "2", required = true)
            @PathVariable Long variantId,
            @Parameter(description = "Nueva cantidad de stock", example = "50", required = true)
            @RequestParam Integer quantity) {
        log.info("REST Request to update stock for Product: {}, Variant: {} to {}", id, variantId, quantity);
        Product product = updateStockUseCase.updateStock(id, variantId, quantity);
        return ResponseEntity.ok(mapper.toResponse(product));
    }

    @GetMapping("/slug/{slug}")
    @Operation(
            operationId = "getProductBySlug",
            summary = "Obtener producto por slug",
            description = "Busca un producto por su URL amigable (SEO). Ideal para el frontend."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content)
    })
    public ResponseEntity<ProductResponse> getProductBySlug(
            @Parameter(description = "Slug SEO del producto", example = "mi-producto", required = true)
            @PathVariable String slug) {
        log.info("REST Request to get Product by Slug : {}", slug);
        return getProductBySlugUseCase.getBySlug(slug)
                .map(product -> ResponseEntity.ok().body(mapper.toResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/variants/{variantId}/availability")
    @RateLimiter(name = "productDetail", fallbackMethod = "variantAvailabilityFallback")
    @Operation(
            operationId = "checkVariantAvailability",
            summary = "Verificar disponibilidad de stock",
            description = "Retorna la cantidad física disponible de una variante. Usado por el Cart Service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad consultada",
                    content = @Content(schema = @Schema(implementation = VariantAvailabilityResponse.class))),
            @ApiResponse(responseCode = "404", description = "Variante no encontrada",
                    content = @Content),
            @ApiResponse(responseCode = "429", description = "Límite de peticiones excedido")
    })
    public ResponseEntity<VariantAvailabilityResponse> checkVariantAvailability(
            @Parameter(description = "ID de la variante", example = "2", required = true)
            @PathVariable Long variantId) {
        log.info("REST Request to check availability for Variant: {}", variantId);
        return checkVariantAvailabilityUseCase.checkAvailability(variantId)
                .map(a -> ResponseEntity.ok(new VariantAvailabilityResponse(
                        String.valueOf(a.variantId()), String.valueOf(a.productId()), a.productName(),
                        a.variantName(), a.sku(), a.available(),
                        a.stockQuantity(), a.productStatus())))
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<VariantAvailabilityResponse> variantAvailabilityFallback(
            Long variantId, Throwable t) {
        log.warn("Rate Limit exceeded for checkVariantAvailability. variantId={}", variantId);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
}
