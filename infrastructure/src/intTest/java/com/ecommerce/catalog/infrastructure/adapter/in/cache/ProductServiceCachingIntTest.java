package com.ecommerce.catalog.infrastructure.adapter.in.cache;

import com.ecommerce.catalog.application.port.in.*;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.model.ProductVariant;
import com.ecommerce.catalog.domain.port.out.ProductRepository;
import com.ecommerce.catalog.domain.port.util.SlugGenerator;
import com.ecommerce.catalog.infrastructure.config.CachingIntTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CachingIntTestConfig.class)
@Testcontainers
class ProductServiceCachingIntTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired private GetProductUseCase getProductUseCase;
    @Autowired private GetProductBySlugUseCase getProductBySlugUseCase;
    @Autowired private ListProductsUseCase listProductsUseCase;
    @Autowired private CreateProductUseCase createProductUseCase;
    @Autowired private CheckVariantAvailabilityUseCase checkVariantAvailabilityUseCase;
    @Autowired private ProductRepository repository;
    @Autowired private SlugGenerator slugGenerator;
    @Autowired private CacheManager cacheManager;

    private Product sampleProduct;
    private static final Long EXISTING_ID = 1L;
    private static final Long NONEXISTENT_ID = 999L;
    private static final String EXISTING_SLUG = "test-product";
    private static final String NONEXISTENT_SLUG = "no-existe";
    private static final Long EXISTING_VARIANT_ID = 101L;
    private static final Long NONEXISTENT_VARIANT_ID = 999L;

    @BeforeEach
    void setUp() {
        reset(repository, slugGenerator);

        sampleProduct = Product.builder()
                .id(EXISTING_ID)
                .skuBase("SKU-TEST")
                .name("Test Product")
                .slug(EXISTING_SLUG)
                .description("Test Description")
                .basePrice(BigDecimal.valueOf(100))
                .currency("USD")
                .status(ProductStatus.ACTIVE)
                .variants(List.of(ProductVariant.builder()
                        .variantId(EXISTING_VARIANT_ID)
                        .sku("SKU-VAR-1")
                        .variantName("Red")
                        .price(BigDecimal.valueOf(100))
                        .currency("USD")
                        .stockQuantity(10)
                        .build()))
                .build();

        evictAllCaches();
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    /**
    En esta prueba de integración se valida que al buscar un producto existente por ID,
    el resultado se almacena en Redis y la segunda llamada retorna desde caché sin invocar al repositorio.

    Características extras:
    - Se utiliza un contenedor Redis real via Testcontainers
    - El repositorio devuelve un producto activo con datos completos

    Se espera que el método:
    - Retorne el producto en ambas llamadas
    - Solo invoque al repositorio una vez (la segunda llamada usa la caché)
    **/
    @Test
    void getById_whenFound_shouldCacheInRedis() {
        //given
        when(repository.findById(EXISTING_ID)).thenReturn(Optional.of(sampleProduct));

        //when
        getProductUseCase.getById(EXISTING_ID);
        getProductUseCase.getById(EXISTING_ID);

        //then
        verify(repository, times(1)).findById(EXISTING_ID);
    }

    /**
    En esta prueba de integración se valida que el producto retornado desde la caché
    contenga los mismos datos que el producto original.

    Características extras:
    - Se invoca getById dos veces seguidas
    - Se verifica que ambas respuestas sean idénticas en contenido

    Se espera que el método:
    - Retorne un Optional presente con el producto correcto
    - El ID del producto sea el mismo en ambas llamadas
    **/
    @Test
    void getById_whenFound_shouldReturnProductFromCache() {
        //given
        when(repository.findById(EXISTING_ID)).thenReturn(Optional.of(sampleProduct));

        //when
        var first = getProductUseCase.getById(EXISTING_ID);
        var second = getProductUseCase.getById(EXISTING_ID);

        //then
        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(first.get().id()).isEqualTo(EXISTING_ID);
        assertThat(second.get().id()).isEqualTo(EXISTING_ID);
    }

    /**
    En esta prueba de integración se valida que al buscar un ID inexistente,
    el Optional vacío se cachea en Redis y la segunda llamada no consulta al repositorio.

    Características extras:
    - El repositorio devuelve Optional.empty() para el ID solicitado
    - La caché almacena el resultado vacío (disableCachingNullValues desactivado)

    Se espera que el método:
    - Retorne Optional.empty() en ambas llamadas
    - Solo invoque al repositorio una vez
    **/
    @Test
    void getById_whenNotFound_shouldReturnEmptyOptional() {
        //given
        when(repository.findById(NONEXISTENT_ID)).thenReturn(Optional.empty());

        //when
        var first = getProductUseCase.getById(NONEXISTENT_ID);
        var second = getProductUseCase.getById(NONEXISTENT_ID);

        //then
        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
        verify(repository, times(1)).findById(NONEXISTENT_ID);
    }

    /**
    En esta prueba de integración se valida que al buscar un ID negativo,
    el servicio retorna Optional.empty() sin errores.

    Características extras:
    - El repositorio devuelve Optional.empty() para IDs negativos
    - No hay validación de ID negativo en el servicio

    Se espera que el método:
    - Retorne Optional.empty()
    - No lance ninguna excepción
    **/
    @Test
    void getById_withNegativeId_shouldReturnEmptyOptional() {
        //given
        Long negativeId = -1L;
        when(repository.findById(negativeId)).thenReturn(Optional.empty());

        //when
        Optional<Product> result = getProductUseCase.getById(negativeId);

        //then
        assertThat(result).isEmpty();
    }

    /**
    En esta prueba de integración se valida que después de limpiar la caché,
    la siguiente llamada al servicio consulta al repositorio nuevamente.

    Características extras:
    - Primero se popula la caché con una llamada exitosa
    - Se limpian todas las cachés manualmente
    - Se vuelve a invocar el servicio

    Se espera que el método:
    - La primera vez cachee el resultado (1 llamada al repositorio)
    - Después de la limpieza, vuelva a consultar al repositorio (2 llamadas totales)
    **/
    @Test
    void getById_afterEviction_shouldHitDatabaseAgain() {
        //given
        when(repository.findById(EXISTING_ID)).thenReturn(Optional.of(sampleProduct));

        //when
        getProductUseCase.getById(EXISTING_ID);
        getProductUseCase.getById(EXISTING_ID);

        //then
        verify(repository, times(1)).findById(EXISTING_ID);

        //when — tras limpiar caché
        evictAllCaches();
        getProductUseCase.getById(EXISTING_ID);

        //then
        verify(repository, times(2)).findById(EXISTING_ID);
    }

    // ─── getBySlug ────────────────────────────────────────────────────────────

    /**
    En esta prueba de integración se valida que al buscar un producto existente por slug,
    el resultado se almacena en Redis y la segunda llamada retorna desde caché.

    Características extras:
    - Se usa un slug válido asociado a un producto activo
    - Se invoca el caso de uso dos veces

    Se espera que el método:
    - Solo invoque al repositorio una vez
    - La segunda llamada se resuelva desde la caché
    **/
    @Test
    void getBySlug_whenFound_shouldCacheInRedis() {
        //given
        when(repository.findBySlug(EXISTING_SLUG)).thenReturn(Optional.of(sampleProduct));

        //when
        getProductBySlugUseCase.getBySlug(EXISTING_SLUG);
        getProductBySlugUseCase.getBySlug(EXISTING_SLUG);

        //then
        verify(repository, times(1)).findBySlug(EXISTING_SLUG);
    }

    /**
    En esta prueba de integración se valida que el producto retornado por slug desde la caché
    contenga los datos correctos del producto original.

    Características extras:
    - Se invoca getBySlug dos veces seguidas
    - Se verifica que ambas respuestas contengan el mismo slug

    Se espera que el método:
    - Retorne un Optional presente con el producto
    - El slug del producto sea el mismo en ambas llamadas
    **/
    @Test
    void getBySlug_whenFound_shouldReturnProductFromCache() {
        //given
        when(repository.findBySlug(EXISTING_SLUG)).thenReturn(Optional.of(sampleProduct));

        //when
        var first = getProductBySlugUseCase.getBySlug(EXISTING_SLUG);
        var second = getProductBySlugUseCase.getBySlug(EXISTING_SLUG);

        //then
        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(first.get().slug()).isEqualTo(EXISTING_SLUG);
    }

    /**
    En esta prueba de integración se valida que al buscar un slug inexistente,
    el Optional vacío se cachea y la segunda llamada no consulta al repositorio.

    Características extras:
    - El repositorio devuelve Optional.empty() para el slug solicitado
    - La caché almacena resultados vacíos

    Se espera que el método:
    - Retorne Optional.empty() en ambas llamadas
    - Solo invoque al repositorio una vez
    **/
    @Test
    void getBySlug_whenNotFound_shouldReturnEmptyOptional() {
        //given
        when(repository.findBySlug(NONEXISTENT_SLUG)).thenReturn(Optional.empty());

        //when
        var first = getProductBySlugUseCase.getBySlug(NONEXISTENT_SLUG);
        var second = getProductBySlugUseCase.getBySlug(NONEXISTENT_SLUG);

        //then
        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
        verify(repository, times(1)).findBySlug(NONEXISTENT_SLUG);
    }

    // ─── list ─────────────────────────────────────────────────────────────────

    /**
    En esta prueba de integración se valida que los resultados paginados de listProducts
    se almacenan en caché por combinación de página y tamaño.

    Características extras:
    - Se consulta con los mismos parámetros de paginación dos veces
    - La página contiene un producto

    Se espera que el método:
    - Solo invoque al repositorio una vez
    - La segunda llamada retorne desde la caché
    **/
    @Test
    void list_shouldCachePagedResult() {
        //given
        var page0 = new com.ecommerce.catalog.domain.model.PagedResult<Product>(
                List.of(sampleProduct), 0, 20, 1L, 1
        );
        when(repository.findAll(0, 20)).thenReturn(page0);

        //when
        listProductsUseCase.list(0, 20);
        listProductsUseCase.list(0, 20);

        //then
        verify(repository, times(1)).findAll(0, 20);
    }

    /**
    En esta prueba de integración se valida que los resultados paginados se cachean
    por clave única de página y tamaño, y que páginas diferentes no comparten caché.

    Características extras:
    - Se consulta con página 0 y tamaño 20
    - Luego se consulta con página 1 y tamaño 20
    - Ambas consultas deben invocar al repositorio

    Se espera que el método:
    - Cada paginación diferente invoque al repositorio una vez
    - No haya compartición de caché entre diferentes páginas
    **/
    @Test
    void list_differentPagination_shouldNotUseCachedResult() {
        //given
        var page0 = new com.ecommerce.catalog.domain.model.PagedResult<Product>(
                List.of(sampleProduct), 0, 20, 1L, 1
        );
        var page1 = new com.ecommerce.catalog.domain.model.PagedResult<Product>(
                List.of(), 1, 20, 1L, 1
        );
        when(repository.findAll(0, 20)).thenReturn(page0);
        when(repository.findAll(1, 20)).thenReturn(page1);

        //when
        listProductsUseCase.list(0, 20);
        listProductsUseCase.list(1, 20);

        //then
        verify(repository, times(1)).findAll(0, 20);
        verify(repository, times(1)).findAll(1, 20);
    }

    // ─── checkAvailability (no cache) ─────────────────────────────────────────

    /**
    En esta prueba de integración se valida que al consultar la disponibilidad de una variante
    existente, se retorna la información correcta de disponibilidad y stock.

    Características extras:
    - La variante existe y tiene stock disponible (10 unidades)
    - checkAvailability no utiliza caché

    Se espera que el método:
    - Retorne un Optional presente con la información de disponibilidad
    - Indique que el producto está disponible (available = true)
    - Reporte el stock correcto de 10 unidades
    **/
    @Test
    void checkAvailability_whenVariantFound_shouldReturnAvailability() {
        //given
        when(repository.findByVariantId(EXISTING_VARIANT_ID)).thenReturn(Optional.of(sampleProduct));

        //when
        var result = checkVariantAvailabilityUseCase.checkAvailability(EXISTING_VARIANT_ID);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().variantId()).isEqualTo(EXISTING_VARIANT_ID);
        assertThat(result.get().available()).isTrue();
        assertThat(result.get().stockQuantity()).isEqualTo(10);
    }

    /**
    En esta prueba de integración se valida que al consultar la disponibilidad de una variante
    inexistente, se retorna Optional.empty().

    Características extras:
    - El ID de variante no existe en el repositorio
    - El servicio retorna vacío sin error

    Se espera que el método:
    - Retorne Optional.empty()
    - No lance ninguna excepción
    **/
    @Test
    void checkAvailability_whenVariantNotFound_shouldReturnEmpty() {
        //given
        when(repository.findByVariantId(NONEXISTENT_VARIANT_ID)).thenReturn(Optional.empty());

        //when
        var result = checkVariantAvailabilityUseCase.checkAvailability(NONEXISTENT_VARIANT_ID);

        //then
        assertThat(result).isEmpty();
    }

    /**
    En esta prueba de integración se valida que al consultar la disponibilidad de una variante
    con stock cero, se retorna que el producto no está disponible.

    Características extras:
    - La variante existe pero tiene stockQuantity = 0
    - checkAvailability no utiliza caché

    Se espera que el método:
    - Retorne un Optional presente con la información de disponibilidad
    - Indique que el producto NO está disponible (available = false)
    - Reporte stockQuantity = 0
    **/
    @Test
    void checkAvailability_whenZeroStock_shouldReturnNotAvailable() {
        //given
        Product zeroStockProduct = sampleProduct.toBuilder()
                .variants(List.of(ProductVariant.builder()
                        .variantId(EXISTING_VARIANT_ID)
                        .sku("SKU-VAR-1")
                        .variantName("Red")
                        .price(BigDecimal.valueOf(100))
                        .currency("USD")
                        .stockQuantity(0)
                        .build()))
                .build();
        when(repository.findByVariantId(EXISTING_VARIANT_ID)).thenReturn(Optional.of(zeroStockProduct));

        //when
        var result = checkVariantAvailabilityUseCase.checkAvailability(EXISTING_VARIANT_ID);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().available()).isFalse();
        assertThat(result.get().stockQuantity()).isZero();
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private void evictAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });
    }
}
