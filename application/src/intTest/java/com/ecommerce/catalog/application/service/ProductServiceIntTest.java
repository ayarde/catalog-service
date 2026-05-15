package com.ecommerce.catalog.application.service;

import com.ecommerce.catalog.application.config.ProductApplicationIntTestConfig;
import com.ecommerce.catalog.application.port.in.GetProductUseCase;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.port.out.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

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
}
