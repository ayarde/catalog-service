package com.ecommerce.catalog.infrastructure.adapter.in.web.mapper;

import com.ecommerce.catalog.application.dto.CreateProductCommand;
import com.ecommerce.catalog.domain.model.PagedResult;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductImage;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.model.ProductVariant;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.PagedResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ProductCreateRequest;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductWebMapperTest {

    private ProductWebMapper webMapper;

    @BeforeEach
    void setUp() {
        webMapper = new ProductWebMapper();
    }

    /**
    En esta prueba unitaria se espera que el mapeo de ProductCreateRequest a
    CreateProductCommand transfiera correctamente todos los datos.

    Características extras:
    - Incluye variantes e imágenes anidadas.
    - Posee atributos y tags.

    Se espera que el método:
    - Cree una instancia de CreateProductCommand.
    - Mapee correctamente campos directos (skuBase, name, description, etc.).
    - Mapee la lista de variantes convirtiéndolas a VariantRequest.
    - Mapee la lista de imágenes convirtiéndolas a ImageRequest.
    **/
    @Test
    void toCommand_WithValidRequest_ReturnsCorrectCommand() {
        //given
        ProductCreateRequest.VariantRequest varReq = new ProductCreateRequest.VariantRequest(
                "VAR-SKU-1", "Red / L", new BigDecimal("120.00"), "USD", 15, Map.of("color", "red")
        );
        ProductCreateRequest.ImageRequest imgReq = new ProductCreateRequest.ImageRequest(
                "https://example.com/image.jpg", "Product Image", 1
        );
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-BASE", "Awesome Product", "Details", new BigDecimal("100.00"), "USD",
                List.of("Electro"), List.of("new"), Map.of("brand", "Acme"),
                List.of(varReq), List.of(imgReq)
        );

        //when
        CreateProductCommand command = webMapper.toCommand(request);

        //then
        assertThat(command).isNotNull();
        assertThat(command.skuBase()).isEqualTo("SKU-BASE");
        assertThat(command.name()).isEqualTo("Awesome Product");
        assertThat(command.description()).isEqualTo("Details");
        assertThat(command.basePrice()).isEqualTo(new BigDecimal("100.00"));
        assertThat(command.currency()).isEqualTo("USD");
        assertThat(command.categories()).containsExactly("Electro");
        assertThat(command.tags()).containsExactly("new");
        assertThat(command.attributes()).containsEntry("brand", "Acme");
        
        assertThat(command.variants()).hasSize(1);
        assertThat(command.variants().get(0).sku()).isEqualTo("VAR-SKU-1");
        assertThat(command.variants().get(0).variantName()).isEqualTo("Red / L");
        assertThat(command.variants().get(0).price()).isEqualTo(new BigDecimal("120.00"));
        assertThat(command.variants().get(0).currency()).isEqualTo("USD");
        assertThat(command.variants().get(0).stockQuantity()).isEqualTo(15);
        assertThat(command.variants().get(0).attributes()).containsEntry("color", "red");

        assertThat(command.images()).hasSize(1);
        assertThat(command.images().get(0).url()).isEqualTo("https://example.com/image.jpg");
        assertThat(command.images().get(0).altText()).isEqualTo("Product Image");
        assertThat(command.images().get(0).sortOrder()).isEqualTo(1);
    }

    /**
    En esta prueba unitaria se espera que el mapeo retorne null si el request de entrada es null,
    garantizando una defensa ante entradas inválidas sin lanzar NullPointerException.

    Características extras:
    - El ProductCreateRequest de entrada es null explícito.

    Se espera que el método:
    - No lance ninguna excepción.
    - Retorne null como resultado del mapeo.
    **/
    @Test
    void toCommand_WithNullRequest_ReturnsNull() {
        //given
        ProductCreateRequest request = null;

        //when
        CreateProductCommand command = webMapper.toCommand(request);

        //then
        assertThat(command).isNull();
    }

    /**
    En esta prueba unitaria se espera que el mapeo de la entidad de dominio Product a
    ProductResponse transfiera correctamente todos los atributos del agregado.

    Características extras:
    - Entidad con datos de control (id, status, timestamps).
    - Incluye colecciones anidadas de dominio.

    Se espera que el método:
    - Retorne un ProductResponse con los datos idénticos de dominio.
    - Formatee el enum status a String.
    **/
    @Test
    void toResponse_WithValidProduct_ReturnsCorrectResponse() {
        //given
        Product product = createTestProduct();

        //when
        ProductResponse response = webMapper.toResponse(product);

        //then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(12345L);
        assertThat(response.skuBase()).isEqualTo("SKU-BASE");
        assertThat(response.name()).isEqualTo("Awesome Product");
        assertThat(response.slug()).isEqualTo("awesome-product");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();

        assertThat(response.variants()).hasSize(1);
        assertThat(response.variants().get(0).variantId()).isEqualTo(999L);
        assertThat(response.variants().get(0).sku()).isEqualTo("VAR-SKU-1");

        assertThat(response.images()).hasSize(1);
        assertThat(response.images().get(0).url()).isEqualTo("https://example.com/image.jpg");
    }

    /**
    En esta prueba unitaria se espera que el mapeo de toCommand maneje correctamente
    la ausencia de variantes, devolviendo una lista vacía en lugar de null.

    Características extras:
    - El ProductCreateRequest tiene variants = null.
    - El ProductCreateRequest tiene images = null.

    Se espera que el método:
    - Retorne un CreateProductCommand con variantes vacías e imágenes vacías.
    **/
    @Test
    void toCommand_WithNullVariantsAndImages_ReturnsEmptyCollections() {
        //given
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-NULL", "No Variants", "Desc", BigDecimal.TEN, "USD",
                List.of(), List.of(), null, null, null
        );

        //when
        CreateProductCommand command = webMapper.toCommand(request);

        //then
        assertThat(command).isNotNull();
        assertThat(command.variants()).isEmpty();
        assertThat(command.images()).isEmpty();
    }

    /**
    En esta prueba unitaria se espera que el mapeo retorne null si el producto de dominio es null,
    garantizando una defensa ante entradas inválidas sin lanzar NullPointerException.

    Características extras:
    - El Product de dominio de entrada es null explícito.

    Se espera que el método:
    - No lance ninguna excepción.
    - Retorne null como resultado del mapeo.
    **/
    @Test
    void toResponse_WithNullProduct_ReturnsNull() {
        //given
        Product product = null;

        //when
        ProductResponse response = webMapper.toResponse(product);

        //then
        assertThat(response).isNull();
    }

    /**
    En esta prueba unitaria se espera que el mapeo de listas mapee correctamente
    cada producto de forma secuencial.

    Características extras:
    - Se proporciona una lista con dos productos de prueba.

    Se espera que el método:
    - Mapee cada elemento a ProductResponse.
    - Devuelva una lista del mismo tamaño y contenido correspondiente.
    **/
    @Test
    void toResponseList_WithProductList_ReturnsResponseList() {
        //given
        Product product1 = createTestProduct();
        Product product2 = createTestProduct();
        List<Product> products = List.of(product1, product2);

        //when
        List<ProductResponse> responses = webMapper.toResponseList(products);

        //then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(12345L);
        assertThat(responses.get(1).id()).isEqualTo(12345L);
    }

    /**
    En esta prueba unitaria se espera que la lista de respuesta sea vacía si la lista de entrada es nula.

    Características extras:
    - La lista de productos de entrada es nula explícitamente.

    Se espera que el método:
    - Retorne una lista de respuestas vacía y no nula.
    **/
    @Test
    void toResponseList_WithNullList_ReturnsEmptyList() {
        //given
        List<Product> products = null;

        //when
        List<ProductResponse> responses = webMapper.toResponseList(products);

        //then
        assertThat(responses).isEmpty();
    }

    /**
    En esta prueba unitaria se espera que toResponse maneje correctamente variantes e imágenes nulas,
    devolviendo colecciones vacías en la respuesta.

    Características extras:
    - Product de dominio con variants = null e images = null.
    - Product de dominio con status = null.

    Se espera que el método:
    - Retorne un ProductResponse con variantes vacías e imágenes vacías.
    - El campo status debe ser null.
    **/
    @Test
    void toResponse_WithNullCollectionsAndStatus_ReturnsEmptyCollections() {
        //given
        Product product = Product.builder()
                .id(1L)
                .skuBase("SKU-NULL")
                .name("No Collections")
                .slug("no-collections")
                .description("Desc")
                .basePrice(BigDecimal.TEN)
                .currency("USD")
                .categories(List.of())
                .tags(List.of())
                .attributes(Map.of())
                .status(null)
                .variants(null)
                .images(null)
                .build();

        //when
        ProductResponse response = webMapper.toResponse(product);

        //then
        assertThat(response).isNotNull();
        assertThat(response.status()).isNull();
        assertThat(response.variants()).isEmpty();
        assertThat(response.images()).isEmpty();
    }

    /**
    En esta prueba unitaria se espera que toPagedResponse retorne null cuando el input es null.

    Características extras:
    - El PagedResult de entrada es null explícitamente.

    Se espera que el método:
    - No lance ninguna excepción.
    - Retorne null.
    **/
    @Test
    void toPagedResponse_WithNullInput_ReturnsNull() {
        //when
        PagedResponse<ProductResponse> result = webMapper.toPagedResponse(null);

        //then
        assertThat(result).isNull();
    }

    /**
    En esta prueba unitaria se espera que toPagedResponse calcule correctamente
    el flag "last" para la última página.

    Características extras:
    - PagedResult con page = 1 y totalPages = 2.

    Se espera que el método:
    - Retorne un PagedResponse con last = true.
    **/
    @Test
    void toPagedResponse_WhenLastPage_ShouldSetLastTrue() {
        //given
        PagedResult<Product> pagedResult = new PagedResult<>(
                List.of(), 1, 20, 0L, 2
        );

        //when
        PagedResponse<ProductResponse> result = webMapper.toPagedResponse(pagedResult);

        //then
        assertThat(result).isNotNull();
        assertThat(result.last()).isTrue();
    }

    /**
    En esta prueba unitaria se espera que toPagedResponse maneje correctamente
    el caso de totalPages = 0.

    Características extras:
    - PagedResult con totalPages = 0.

    Se espera que el método:
    - Retorne un PagedResponse con last = true.
    **/
    @Test
    void toPagedResponse_WhenTotalPagesIsZero_ShouldSetLastTrue() {
        //given
        PagedResult<Product> pagedResult = new PagedResult<>(
                List.of(), 0, 20, 0L, 0
        );

        //when
        PagedResponse<ProductResponse> result = webMapper.toPagedResponse(pagedResult);

        //then
        assertThat(result).isNotNull();
        assertThat(result.last()).isTrue();
    }

    /**
    En esta prueba unitaria se espera que toPagedResponse devuelva una página intermedia
    con last = false.

    Características extras:
    - PagedResult con page = 0 y totalPages = 3 (no es la última).

    Se espera que el método:
    - Retorne un PagedResponse con last = false.
    **/
    @Test
    void toPagedResponse_WhenNotLastPage_ShouldSetLastFalse() {
        //given
        PagedResult<Product> pagedResult = new PagedResult<>(
                List.of(), 0, 20, 60L, 3
        );

        //when
        PagedResponse<ProductResponse> result = webMapper.toPagedResponse(pagedResult);

        //then
        assertThat(result).isNotNull();
        assertThat(result.last()).isFalse();
    }

    //<editor-fold desc="Métodos auxiliares">
    private Product createTestProduct() {
        ProductVariant variant = ProductVariant.builder()
                .variantId(999L)
                .sku("VAR-SKU-1")
                .variantName("Variant 1")
                .price(new BigDecimal("120.00"))
                .currency("USD")
                .stockQuantity(10)
                .attributes(Map.of("color", "red"))
                .build();

        ProductImage image = ProductImage.builder()
                .url("https://example.com/image.jpg")
                .altText("Alt Text")
                .sortOrder(0)
                .build();

        return Product.builder()
                .id(12345L)
                .skuBase("SKU-BASE")
                .name("Awesome Product")
                .slug("awesome-product")
                .description("Details")
                .basePrice(new BigDecimal("100.00"))
                .currency("USD")
                .categories(List.of("Electro"))
                .tags(List.of("new"))
                .attributes(Map.of("brand", "Acme"))
                .status(ProductStatus.ACTIVE)
                .variants(List.of(variant))
                .images(List.of(image))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    //</editor-fold>
}
