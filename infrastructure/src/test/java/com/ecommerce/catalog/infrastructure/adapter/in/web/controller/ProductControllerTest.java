package com.ecommerce.catalog.infrastructure.adapter.in.web.controller;

import com.ecommerce.catalog.application.dto.VariantAvailability;
import com.ecommerce.catalog.application.port.in.*;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ProductCreateRequest;
import com.ecommerce.catalog.infrastructure.adapter.in.web.mapper.ProductWebMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock private CreateProductUseCase createProductUseCase;
    @Mock private UpdateProductUseCase updateProductUseCase;
    @Mock private ActivateProductUseCase activateProductUseCase;
    @Mock private DeleteProductUseCase deleteProductUseCase;
    @Mock private GetProductUseCase getProductUseCase;
    @Mock private ListProductsUseCase listProductsUseCase;
    @Mock private GetProductBySlugUseCase getProductBySlugUseCase;
    @Mock private UpdateStockUseCase updateStockUseCase;
    @Mock private CheckVariantAvailabilityUseCase checkVariantAvailabilityUseCase;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(
                createProductUseCase, updateProductUseCase, activateProductUseCase, deleteProductUseCase,
                getProductUseCase, listProductsUseCase, getProductBySlugUseCase, updateStockUseCase,
                checkVariantAvailabilityUseCase, new ProductWebMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
    En esta prueba unitaria se espera que se valide la creación de un producto a través de una petición REST válida.

    Características extras:
    - La petición contiene todos los datos requeridos estructurados en formato JSON.
    - Se utiliza un caso de uso mockeado (CreateProductUseCase).

    Se espera que el método:
    - Retorne un estado HTTP 201 (Created).
    - Devuelva el ID del producto creado correctamente en la respuesta JSON.
    **/
    @Test
    void createProduct_WhenValidRequest_ShouldReturn201() throws Exception {
        //given
        Product product = sampleProduct(1L);
        when(createProductUseCase.create(any())).thenReturn(product);
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-1", "Product", "Desc", BigDecimal.TEN, "USD",
                List.of(), List.of(), null, List.of(), List.of()
        );

        //when / then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"));
    }

    /**
    En esta prueba unitaria se espera que se valide la obtención de un producto por su ID cuando este existe en el sistema.

    Características extras:
    - El producto existe previamente y el caso de uso devuelve un opcional con la entidad de dominio.

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK).
    - Devuelva el SKU base del producto esperado en la respuesta JSON.
    **/
    @Test
    void getProduct_WhenExists_ShouldReturn200() throws Exception {
        //given
        when(getProductUseCase.getById(1L)).thenReturn(Optional.of(sampleProduct(1L)));

        //when / then
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuBase").value("SKU-1"));
    }

    /**
    En esta prueba unitaria se espera que se valide que la búsqueda de un producto por ID retorne 404 si no existe en el sistema.

    Características extras:
    - El caso de uso devuelve un opcional vacío.

    Se espera que el método:
    - Retorne un estado HTTP 404 (Not Found).
    **/
    @Test
    void getProduct_WhenNotFound_ShouldReturn404() throws Exception {
        //given
        when(getProductUseCase.getById(99L)).thenReturn(Optional.empty());

        //when / then
        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound());
    }

    /**
    En esta prueba unitaria se espera que se valide la obtención de la lista paginada de productos.

    Características extras:
    - Se retorna una página con al menos un producto mockeado.

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK).
    - Devuelva la página de productos conteniendo el ID del producto esperado y los metadatos correspondientes.
    **/
    @Test
    void listProducts_WhenCalled_ShouldReturnPagedResponse() throws Exception {
        //given
        com.ecommerce.catalog.domain.model.PagedResult<Product> pagedResult = new com.ecommerce.catalog.domain.model.PagedResult<>(
                List.of(sampleProduct(1L)), 0, 20, 1L, 1
        );
        when(listProductsUseCase.list(0, 20)).thenReturn(pagedResult);

        //when / then
        mockMvc.perform(get("/api/v1/products?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1L));
    }

    /**
    En esta prueba unitaria se espera que se valide la activación de un producto existente.

    Características extras:
    - El producto es activado correctamente a través de la capa de aplicación.

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK).
    **/
    @Test
    void activateProduct_WhenExists_ShouldReturn200() throws Exception {
        //given
        when(activateProductUseCase.activate(any())).thenReturn(sampleProduct(1L));

        //when / then
        mockMvc.perform(put("/api/v1/products/1/activate"))
                .andExpect(status().isOk());
    }

    /**
    En esta prueba unitaria se espera que se valide la actualización y posterior eliminación de un producto mediante peticiones HTTP.

    Características extras:
    - Se realizan dos llamadas consecutivas en la misma prueba (PUT y DELETE).

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK) al actualizar.
    - Retorne un estado HTTP 204 (No Content) al eliminar.
    **/
    @Test
    void updateAndDeleteProduct_WhenCalled_ShouldReturnExpectedStatus() throws Exception {
        //given
        when(updateProductUseCase.update(any())).thenReturn(sampleProduct(1L));
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-1", "Product", "Desc", BigDecimal.TEN, "USD",
                List.of(), List.of(), null, List.of(), List.of()
        );

        //when / then
        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }

    /**
    En esta prueba unitaria se espera que se valide la actualización de stock de una variante de producto.

    Características extras:
    - Se envía la cantidad de stock a actualizar como parámetro de consulta (query parameter).

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK).
    **/
    @Test
    void updateStock_WhenCalled_ShouldReturn200() throws Exception {
        //given
        when(updateStockUseCase.updateStock(1L, 10L, 3)).thenReturn(sampleProduct(1L));

        //when / then
        mockMvc.perform(patch("/api/v1/products/1/variants/10/stock").param("quantity", "3"))
                .andExpect(status().isOk());
    }

    /**
    En esta prueba unitaria se espera que se valide la obtención de un producto utilizando su slug.

    Características extras:
    - El producto con el slug solicitado está registrado en el sistema.

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK).
    **/
    @Test
    void getProductBySlug_WhenExists_ShouldReturn200() throws Exception {
        //given
        when(getProductBySlugUseCase.getBySlug("my-slug")).thenReturn(Optional.of(sampleProduct(1L)));

        //when / then
        mockMvc.perform(get("/api/v1/products/slug/my-slug"))
                .andExpect(status().isOk());
    }

    /**
    En esta prueba unitaria se espera que se valide la verificación de disponibilidad de una variante.

    Características extras:
    - La variante existe y se retorna su disponibilidad con datos simulados.

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK).
    - Devuelva la disponibilidad y el ID de la variante correctos en el cuerpo de la respuesta.
    **/
    @Test
    void checkVariantAvailability_WhenExists_ShouldReturn200() throws Exception {
        //given
        when(checkVariantAvailabilityUseCase.checkAvailability(10L))
                .thenReturn(Optional.of(new VariantAvailability(
                        10L, 1L, "Product", "Variant", "V-SKU", true, 5, "ACTIVE"
                )));

        //when / then
        mockMvc.perform(get("/api/v1/products/variants/10/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variantId").value("10"));
    }

    /**
    En esta prueba unitaria se espera que se valide el comportamiento de fallback para el listado de productos ante límite de tasa (rate limit).

    Características extras:
    - Se simula el lanzamiento de una excepción por rate limit.

    Se espera que el método:
    - Retorne una página vacía como respuesta de respaldo.
    **/
    @Test
    void listProductsFallback_WhenRateLimited_ShouldReturnEmptyPage() {
        //given
        ProductController controller = new ProductController(
                createProductUseCase, updateProductUseCase, activateProductUseCase, deleteProductUseCase,
                getProductUseCase, listProductsUseCase, getProductBySlugUseCase, updateStockUseCase,
                checkVariantAvailabilityUseCase, new ProductWebMapper()
        );

        //when
        com.ecommerce.catalog.infrastructure.adapter.in.web.dto.PagedResponse<?> result = controller.listProductsFallback(0, 20, new RuntimeException("limit"));

        //then
        assertThat(result.content()).isEmpty();
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);
    }

    /**
    En esta prueba unitaria se espera que se valide el comportamiento de fallback para verificar disponibilidad de variante ante límite de tasa.

    Características extras:
    - Se simula el lanzamiento de una excepción por rate limit.

    Se espera que el método:
    - Retorne una respuesta con estado HTTP 429 (Too Many Requests).
    **/
    @Test
    void variantAvailabilityFallback_WhenRateLimited_ShouldReturn429() {
        //given
        ProductController controller = new ProductController(
                createProductUseCase, updateProductUseCase, activateProductUseCase, deleteProductUseCase,
                getProductUseCase, listProductsUseCase, getProductBySlugUseCase, updateStockUseCase,
                checkVariantAvailabilityUseCase, new ProductWebMapper()
        );

        //when
        var response = controller.variantAvailabilityFallback(10L, new RuntimeException("limit"));

        //then
        assertThat(response.getStatusCode().value()).isEqualTo(429);
    }

    /**
    En esta prueba unitaria se espera que la búsqueda por slug retorne 404 cuando el slug no existe en el sistema.

    Características extras:
    - El caso de uso devuelve un Optional.empty() para el slug solicitado.

    Se espera que el método:
    - Retorne un estado HTTP 404 (Not Found).
    **/
    @Test
    void getProductBySlug_WhenNotFound_ShouldReturn404() throws Exception {
        //given
        when(getProductBySlugUseCase.getBySlug("no-existe")).thenReturn(Optional.empty());

        //when / then
        mockMvc.perform(get("/api/v1/products/slug/no-existe"))
                .andExpect(status().isNotFound());
    }

    /**
    En esta prueba unitaria se espera que la verificación de disponibilidad retorne 404 cuando la variante no existe.

    Características extras:
    - El caso de uso devuelve Optional.empty() para el ID de variante solicitado.

    Se espera que el método:
    - Retorne un estado HTTP 404 (Not Found).
    **/
    @Test
    void checkVariantAvailability_WhenNotFound_ShouldReturn404() throws Exception {
        //given
        when(checkVariantAvailabilityUseCase.checkAvailability(999L)).thenReturn(Optional.empty());

        //when / then
        mockMvc.perform(get("/api/v1/products/variants/999/availability"))
                .andExpect(status().isNotFound());
    }

    /**
    En esta prueba unitaria se espera que la verificación de disponibilidad retorne correctamente
    que una variante con stock cero no está disponible.

    Características extras:
    - La variante existe pero su stock es 0.
    - Se consulta el endpoint de disponibilidad.

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK).
    - available debe ser false.
    - stockQuantity debe ser 0.
    **/
    @Test
    void checkVariantAvailability_WhenZeroStock_ShouldReturnNotAvailable() throws Exception {
        //given
        when(checkVariantAvailabilityUseCase.checkAvailability(10L))
                .thenReturn(Optional.of(new VariantAvailability(
                        10L, 1L, "Product", "Variant", "V-SKU", false, 0, "ACTIVE"
                )));

        //when / then
        mockMvc.perform(get("/api/v1/products/variants/10/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.stockQuantity").value(0));
    }

    //<editor-fold desc="Métodos auxiliares">
    private Product sampleProduct(Long id) {
        return Product.builder()
                .id(id)
                .skuBase("SKU-1")
                .name("Product")
                .slug("product")
                .basePrice(BigDecimal.TEN)
                .currency("USD")
                .status(ProductStatus.DRAFT)
                .build();
    }
    //</editor-fold>
}
