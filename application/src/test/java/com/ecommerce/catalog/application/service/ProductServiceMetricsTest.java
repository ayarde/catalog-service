package com.ecommerce.catalog.application.service;

import com.ecommerce.catalog.application.dto.ActivateProductCommand;
import com.ecommerce.catalog.application.dto.CreateProductCommand;
import com.ecommerce.catalog.application.dto.ImageRequest;
import com.ecommerce.catalog.application.dto.VariantRequest;
import com.ecommerce.catalog.domain.exception.AlreadyExistsException;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductImage;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.model.ProductVariant;
import com.ecommerce.catalog.domain.port.out.EventPublisher;
import com.ecommerce.catalog.domain.port.out.ProductRepository;
import com.ecommerce.catalog.domain.port.util.SlugGenerator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceMetricsTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private SlugGenerator slugGenerator;

    private MeterRegistry meterRegistry;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        productService = new ProductService(repository, eventPublisher, slugGenerator, meterRegistry);
    }

    /**
    En esta prueba unitaria se espera que se incremente el contador de productos creados
    cuando se crea un producto con SKU único.

    Características extras:
    - El SKU no existe previamente en el repositorio.
    - Se proporcionan datos completos del producto.

    Se espera que el método:
    - Incremente el contador catalog_products_created_total en 1.
    - Persista el producto en el repositorio.
    **/
    @Test
    void create_WithUniqueSku_IncrementsProductsCreatedCounter() {
        //given
        when(repository.existsBySku(anyString())).thenReturn(false);
        when(slugGenerator.generate(anyString())).thenReturn("test-slug");
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        productService.create(new CreateProductCommand(
                "SKU-001", "Test", "Desc", BigDecimal.TEN, "USD",
                List.of("Cat1"), List.of("Tag1"), Map.of(),
                List.of(new VariantRequest("V-SKU", "V-Name", BigDecimal.TEN, "USD", 5, Map.of())),
                List.of(new ImageRequest("https://url.com/img.jpg", "alt", 0))
        ));

        //then
        double count = meterRegistry.get("catalog_products_created_total").counter().count();
        assertThat(count).isEqualTo(1);
    }

    /**
    En esta prueba unitaria se espera que se incremente el contador de SKUs duplicados
    cuando se intenta crear un producto con un SKU que ya existe.

    Características extras:
    - El SKU ya existe en la base de datos.
    - Se lanza AlreadyExistsException.

    Se espera que el método:
    - Incremente el contador catalog_validation_duplicate_sku_total en 1.
    - Lance AlreadyExistsException.
    - No persista el producto en el repositorio.
    **/
    @Test
    void create_WithDuplicateSku_IncrementsDuplicateSkuCounter() {
        //given
        when(repository.existsBySku("DUP-SKU")).thenReturn(true);

        //when
        assertThrows(AlreadyExistsException.class, () ->
                productService.create(new CreateProductCommand(
                        "DUP-SKU", "Test", "Desc", BigDecimal.ONE, "USD",
                        null, null, null, null, null
                ))
        );

        //then
        double count = meterRegistry.get("catalog_validation_duplicate_sku_total").counter().count();
        assertThat(count).isEqualTo(1);
    }

    /**
    En esta prueba unitaria se espera que se incremente el contador de productos activados
    cuando un producto en estado DRAFT se activa exitosamente.

    Características extras:
    - El producto existe y cumple con todas las validaciones para activación.
    - El producto tiene precio, moneda, variantes e imágenes.

    Se espera que el método:
    - Incremente el contador catalog_products_activated_total en 1.
    - Cambie el estado del producto a ACTIVE.
    **/
    @Test
    void activate_WithValidProduct_IncrementsProductsActivatedCounter() {
        //given
        Product draftProduct = Product.builder()
                .id(1L).skuBase("SKU").name("Test").slug("test")
                .basePrice(BigDecimal.TEN).currency("USD")
                .status(ProductStatus.DRAFT)
                .variants(List.of(ProductVariant.builder()
                        .variantId(101L).sku("V-SKU").variantName("Default")
                        .price(BigDecimal.TEN).currency("USD").stockQuantity(10).build()))
                .images(List.of(new ProductImage("https://url.com/img.jpg", "alt", 0)))
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(draftProduct));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        productService.activate(new ActivateProductCommand(1L));

        //then
        double count = meterRegistry.get("catalog_products_activated_total").counter().count();
        assertThat(count).isEqualTo(1);
    }

    /**
    En esta prueba unitaria se espera que se actualice el gauge de productos fuera de stock
    cuando el stock de una variante se reduce a cero.

    Características extras:
    - El producto está en estado ACTIVE.
    - Se actualiza el stock de la variante a 0.

    Se espera que el método:
    - Cambie el estado del producto a OUT_OF_STOCK.
    - Actualice el gauge catalog_stock_out_of_stock a 1.
    **/
    @Test
    void updateStock_WithZeroQuantity_UpdatesOutOfStockGauge() {
        //given
        Product activeProduct = Product.builder()
                .id(1L).skuBase("SKU").name("Test").slug("test")
                .basePrice(BigDecimal.TEN).currency("USD")
                .status(ProductStatus.ACTIVE)
                .variants(List.of(ProductVariant.builder()
                        .variantId(101L).sku("V-SKU").variantName("Default")
                        .price(BigDecimal.TEN).currency("USD").stockQuantity(10).build()))
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(activeProduct));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        productService.updateStock(1L, 101L, 0);

        //then
        double gaugeValue = meterRegistry.get("catalog_stock_out_of_stock").gauge().value();
        assertThat(gaugeValue).isEqualTo(1);
    }
}
