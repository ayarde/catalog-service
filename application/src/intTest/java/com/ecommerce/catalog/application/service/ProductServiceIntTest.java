package com.ecommerce.catalog.application.service;

import com.ecommerce.catalog.application.config.ProductApplicationIntTestConfig;
import com.ecommerce.catalog.application.port.in.GetProductBySlugUseCase;
import com.ecommerce.catalog.application.port.in.GetProductUseCase;
import com.ecommerce.catalog.application.port.in.ListProductsUseCase;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.port.out.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Prueba de integración para validar la infraestructura de caché en el servicio de productos.
 */
@SpringBootTest(classes = ProductApplicationIntTestConfig.class)
public class ProductServiceIntTest {

    @Autowired
    private GetProductUseCase getProductUseCase;

    @Autowired
    private GetProductBySlugUseCase getProductBySlugUseCase;

    @Autowired
    private ListProductsUseCase listProductsUseCase;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CacheManager cacheManager;

    /**
    En esta prueba de integración se valida que la infraestructura de caché esté operando correctamente
    para la obtención de productos por ID.
    
    Características extras:
    - Se utiliza un ConcurrentMapCacheManager simulado para el test
    - Se verifica que la segunda llamada no invoque al repositorio
    
    Se espera que el método:
    - Retorne el producto desde el repositorio en la primera llamada
    - Retorne el producto desde la caché en llamadas subsecuentes
    **/
    @Test
    public void getById_SuccessCaching() {
        //given
        // Configuración de datos de prueba e ID de producto
        Long productId = 1L;
        Product mockProduct = Product.builder()
                .id(productId)
                .skuBase("SKUBASE")
                .name("ProductName")
                .slug("productname")
                .status(ProductStatus.ACTIVE)
                .build();

        when(repository.findById(productId)).thenReturn(Optional.of(mockProduct));

        //when
        // Invocación doble del caso de uso para validar el comportamiento de la caché
        getProductUseCase.getById(productId);
        getProductUseCase.getById(productId);

        //then
        // Verificación de que el repositorio solo fue llamado una vez y la caché existe
        verify(repository, times(1)).findById(productId);
        assertThat(cacheManager.getCache("product")).as("El gestor de caché debe contener la caché de productos").isNotNull();
    }

    /**
    En esta prueba de integración se valida que al buscar un ID inexistente,
    el Optional vacío se almacena en la caché y la segunda llamada no consulta al repositorio.

    Características extras:
    - El repositorio devuelve Optional.empty() para el ID 99L
    - La caché almacena el resultado vacío (disableCachingNullValues desactivado)

    Se espera que el método:
    - Retorne Optional.empty() en ambas llamadas
    - Solo invoque al repositorio una vez (la segunda llamada usa la caché)
    **/
    @Test
    public void getById_WhenNotFound_ShouldReturnEmptyOptional() {
        //given
        Long productId = 99L;
        when(repository.findById(productId)).thenReturn(Optional.empty());

        //when
        var first = getProductUseCase.getById(productId);
        var second = getProductUseCase.getById(productId);

        //then
        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
        verify(repository, times(1)).findById(productId);
    }

    @Test
    public void getBySlug_SuccessCaching() {
        String slug = "test-slug";
        Product mockProduct = Product.builder()
                .id(1L).skuBase("SKU").name("P").slug(slug)
                .status(ProductStatus.ACTIVE).build();

        when(repository.findBySlug(slug)).thenReturn(Optional.of(mockProduct));

        getProductBySlugUseCase.getBySlug(slug);
        getProductBySlugUseCase.getBySlug(slug);

        verify(repository, times(1)).findBySlug(slug);
        assertThat(cacheManager.getCache("product_slug")).isNotNull();
    }

    /**
    En esta prueba de integración se valida que al buscar un slug inexistente,
    el Optional vacío se almacena en la caché y la segunda llamada no consulta al repositorio.

    Características extras:
    - El repositorio devuelve Optional.empty() para el slug "no-existe"
    - La caché de slugs almacena el resultado vacío

    Se espera que el método:
    - Retorne Optional.empty() en ambas llamadas
    - Solo invoque al repositorio una vez
    **/
    @Test
    public void getBySlug_WhenNotFound_ShouldReturnEmptyOptional() {
        //given
        String slug = "no-existe";
        when(repository.findBySlug(slug)).thenReturn(Optional.empty());

        //when
        var first = getProductBySlugUseCase.getBySlug(slug);
        var second = getProductBySlugUseCase.getBySlug(slug);

        //then
        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
        verify(repository, times(1)).findBySlug(slug);
    }

    @Test
    public void list_SuccessCaching() {
        var pagedResult = new com.ecommerce.catalog.domain.model.PagedResult<Product>(
                List.of(), 0, 20, 0L, 0
        );
        int page = 0, size = 20;

        when(repository.findAll(page, size))
                .thenReturn(pagedResult);

        var result1 = listProductsUseCase.list(page, size);
        var result2 = listProductsUseCase.list(page, size);

        verify(repository, times(1)).findAll(page, size);
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
    }

    // El test de evicción por creación se encuentra en ProductServiceCachingIntTest
    // (infrastructure/intTest) usando Redis real via Testcontainers, donde se puede
    // verificar el cache miss post-evicción con el flujo completo.
}
