package com.ecommerce.catalog.application.service;

import com.ecommerce.catalog.application.dto.*;
import com.ecommerce.catalog.domain.exception.AlreadyExistsException;
import com.ecommerce.catalog.domain.exception.NotFoundException;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.model.ProductVariant;
import com.ecommerce.catalog.domain.port.out.EventPublisher;
import com.ecommerce.catalog.domain.port.out.ProductRepository;
import com.ecommerce.catalog.domain.port.util.SlugGenerator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de productos siguiendo estrictamente TESTING_RULES.md.
 */
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private SlugGenerator slugGenerator;

    private ProductService productService;
    private Product testProduct;

    @BeforeEach
    public void setUp() {
        productService = new ProductService(repository, eventPublisher, slugGenerator, new SimpleMeterRegistry());
        testProduct = Product.builder()
                .id(1L)
                .skuBase("SKU-BASE")
                .name("Test Product")
                .slug("test-product-sku-base")
                .basePrice(BigDecimal.valueOf(100))
                .currency("USD")
                .status(ProductStatus.DRAFT)
                .variants(List.of(
                        ProductVariant.builder()
                                .variantId(101L)
                                .sku("SKU-VAR-1")
                                .variantName("Red")
                                .price(BigDecimal.valueOf(100))
                                .currency("USD")
                                .stockQuantity(10)
                                .build()
                ))
                .images(List.of(
                        new com.ecommerce.catalog.domain.model.ProductImage("https://url.com/img.jpg", "alt", 0)
                ))
                .build();
    }

    /**
    En esta prueba unitaria se espera que se cree un producto exitosamente cuando el SKU es único.
    
    Características extras:
    - El SKU no existe en el repositorio
    - Se genera un slug válido automáticamente
    - Se proporcionan datos completos de variantes e imágenes
    
    Se espera que el método:
    - Valide la unicidad del SKU
    - Persista el producto en el repositorio
    - Publique los eventos de dominio correspondientes
    **/
    @Test
    public void create_NewProductWithUniqueSku_SavesToRepository() {
        //given
        CreateProductCommand command = new CreateProductCommand(
                "FULL-SKU", "Full Product", "Desc", BigDecimal.TEN, "USD",
                List.of("Cat1"), List.of("Tag1"), Map.of("Color", "Red"),
                List.of(new VariantRequest("V-SKU", "V-Name", BigDecimal.TEN, "USD", 5, Map.of())),
                List.of(new ImageRequest("https://url.com/img.jpg", "alt", 0))
        );
        when(repository.existsBySku(anyString())).thenReturn(false);
        when(slugGenerator.generate(anyString())).thenReturn("full-product-full-sku");
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        Product result = productService.create(command);

        //then
        assertThat(result.variants()).as("Debería haberse creado una variante").hasSize(1);
        assertThat(result.images()).as("Debería haberse asociado una imagen").hasSize(1);
        verify(repository).save(any());
    }

    /**
    En esta prueba se espera que se lance una excepción cuando se intenta crear un producto con un SKU duplicado.
    
    Características extras:
    - El SKU ya existe en la base de datos
    
    Se espera que el método:
    - Lance AlreadyExistsException
    - No intente guardar el producto en el repositorio
    **/
    @Test
    public void create_DuplicateSku_ThrowsAlreadyExistsException() {
        //given
        CreateProductCommand command = new CreateProductCommand("EXISTS", "T", "D", BigDecimal.ONE, "USD", 
            null, null, null, null, null);
        when(repository.existsBySku("EXISTS")).thenReturn(true);

        //when
        try {
            productService.create(command);
            fail("Debería haber lanzado AlreadyExistsException");
        } catch (AlreadyExistsException e) {
            //then
            assertThat(e.getMessage()).as("El mensaje de error debería ser correcto").contains("error.product.already_exists");
            verify(repository, never()).save(any());
        }
    }

    /**
    En esta prueba se valida la actualización de los datos básicos de un producto.
    
    Características extras:
    - El producto existe previamente
    - Se actualizan nombre y precio
    
    Se espera que el método:
    - Recupere el producto original
    - Guarde los cambios en el repositorio
    - Publique eventos de actualización
    **/
    @Test
    public void update_ExistingProduct_UpdatesCorrectly() {
        //given
        UpdateProductCommand command = new UpdateProductCommand(
                1L, "New Name", "New Desc", BigDecimal.valueOf(150), "EUR", List.of(), List.of()
        );
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(repository.save(any(Product.class))).thenReturn(testProduct);

        //when
        Product result = productService.update(command);

        //then
        assertThat(result).as("El resultado de la actualización no debe ser nulo").isNotNull();
        verify(eventPublisher).publish(anyList());
    }

    /**
    En esta prueba se espera que falle la actualización si el ID del producto no existe.
    
    Características extras:
    - Se utiliza un ID inexistente
    
    Se espera que el método:
    - Lance NotFoundException
    - Indique el código de error correspondiente
    **/
    @Test
    public void update_NonExistentProduct_ThrowsNotFoundException() {
        //given
        UpdateProductCommand command = new UpdateProductCommand(99L, "X", "D", BigDecimal.ONE, "USD", null, null);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        //when
        try {
            productService.update(command);
            fail("Debería haber lanzado NotFoundException");
        } catch (NotFoundException e) {
            //then
            assertThat(e.getMessage()).as("El mensaje debe contener el código de error").contains("error.product.not_found");
        }
    }

    /**
    En esta prueba se valida que el stock de una variante se actualice sin alterar el estado del producto.
    
    Características extras:
    - El producto tiene estado ACTIVE
    - El nuevo stock es mayor que cero
    
    Se espera que el método:
    - Actualice la cantidad de stock de la variante
    - Mantenga el estado ACTIVE del producto
    **/
    @Test
    public void updateStock_AvailableQuantity_MaintainsActiveStatus() {
        //given
        Product activeProduct = testProduct.toBuilder().status(ProductStatus.ACTIVE).build();
        when(repository.findById(1L)).thenReturn(Optional.of(activeProduct));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        Product result = productService.updateStock(1L, 101L, 5);

        //then
        assertThat(result.status()).as("El estado debe seguir siendo ACTIVE").isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.findVariantById(101L).get().stockQuantity()).as("El stock debe ser 5").isEqualTo(5);
        verify(repository).save(any());
    }

    /**
    En esta prueba se valida que el producto pase a OUT_OF_STOCK cuando el stock de sus variantes llega a cero.
    
    Características extras:
    - El producto está inicialmente ACTIVE
    - Se establece el stock en 0
    
    Se espera que el método:
    - Detecte que no hay stock disponible
    - Cambie el estado del producto a OUT_OF_STOCK
    **/
    @Test
    public void updateStock_ZeroQuantity_ChangesStatusToOutOfStock() {
        //given
        Product activeProduct = testProduct.toBuilder().status(ProductStatus.ACTIVE).build();
        when(repository.findById(1L)).thenReturn(Optional.of(activeProduct));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        Product result = productService.updateStock(1L, 101L, 0);

        //then
        assertThat(result.status()).as("Debe cambiar a fuera de stock").isEqualTo(ProductStatus.OUT_OF_STOCK);
    }

    /**
    En esta prueba se valida la recuperación de disponibilidad de una variante.
    
    Características extras:
    - Se consulta por ID de variante
    - El producto asociado existe y tiene stock
    
    Se espera que el método:
    - Retorne un DTO con la información de disponibilidad
    - Incluya datos del producto padre y la variante
    **/
    @Test
    public void checkAvailability_ExistingVariant_ReturnsFullAvailabilityInfo() {
        //given
        when(repository.findByVariantId(101L)).thenReturn(Optional.of(testProduct));

        //when
        Optional<VariantAvailability> result = productService.checkAvailability(101L);

        //then
        assertThat(result).as("La disponibilidad debe estar presente").isPresent();
        VariantAvailability availability = result.get();
        assertThat(availability.productId()).as("ID de producto correcto").isEqualTo(1L);
        assertThat(availability.available()).as("Debe indicar que está disponible").isTrue();
    }

    /**
    En esta prueba se valida la obtención paginada de los productos del catálogo.
    
    Características extras:
    - Existen productos registrados
    
    Se espera que el método:
    - Retorne el resultado paginado con la información de los productos.
    **/
    @Test
    public void list_ExistingProducts_ReturnsPagedResult() {
        //given
        com.ecommerce.catalog.domain.model.PagedResult<Product> pagedResult = new com.ecommerce.catalog.domain.model.PagedResult<>(
                List.of(testProduct), 0, 20, 1L, 1
        );
        when(repository.findAll(0, 20)).thenReturn(pagedResult);
        
        //when
        com.ecommerce.catalog.domain.model.PagedResult<Product> result = productService.list(0, 20);
        
        //then
        assertThat(result.content()).as("La lista no debe estar vacía").isNotEmpty();
        assertThat(result.totalElements()).isEqualTo(1L);
        verify(repository).findAll(0, 20);
    }

    /**
    En esta prueba unitaria se espera que se recupere un producto existente por su ID.

    Características extras:
    - El producto existe en el repositorio.
    - Se consulta con un ID válido.

    Se espera que el método:
    - Retorne un Optional presente con el producto.
    - El ID del producto coincida con el solicitado.
    **/
    @Test
    public void getById_WhenExistingId_ReturnsProduct() {
        //given
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));

        //when
        Optional<Product> result = productService.getById(1L);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        verify(repository).findById(1L);
    }

    /**
    En esta prueba unitaria se espera que se retorne Optional.empty() al buscar un ID inexistente.

    Características extras:
    - El repositorio no encuentra un producto con el ID solicitado.

    Se espera que el método:
    - Retorne Optional.empty().
    **/
    @Test
    public void getById_WhenNonExistentId_ReturnsEmptyOptional() {
        //given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        //when
        Optional<Product> result = productService.getById(99L);

        //then
        assertThat(result).isEmpty();
        verify(repository).findById(99L);
    }

    /**
    En esta prueba unitaria se espera que se recupere un producto existente por su slug.

    Características extras:
    - El producto existe en el repositorio.
    - Se consulta con un slug válido.

    Se espera que el método:
    - Retorne un Optional presente con el producto.
    - El slug del producto coincida con el solicitado.
    **/
    @Test
    public void getBySlug_WhenExistingSlug_ReturnsProduct() {
        //given
        when(repository.findBySlug("test-product-sku-base")).thenReturn(Optional.of(testProduct));

        //when
        Optional<Product> result = productService.getBySlug("test-product-sku-base");

        //then
        assertThat(result).isPresent();
        assertThat(result.get().slug()).isEqualTo("test-product-sku-base");
        verify(repository).findBySlug("test-product-sku-base");
    }

    /**
    En esta prueba unitaria se espera que se retorne Optional.empty() al buscar un slug inexistente.

    Características extras:
    - El repositorio no encuentra un producto con el slug solicitado.

    Se espera que el método:
    - Retorne Optional.empty().
    **/
    @Test
    public void getBySlug_WhenNonExistentSlug_ReturnsEmptyOptional() {
        //given
        when(repository.findBySlug("no-existe")).thenReturn(Optional.empty());

        //when
        Optional<Product> result = productService.getBySlug("no-existe");

        //then
        assertThat(result).isEmpty();
        verify(repository).findBySlug("no-existe");
    }

    /**
    En esta prueba unitaria se espera que se retorne Optional.empty() al consultar
    la disponibilidad de una variante que no existe.

    Características extras:
    - El ID de variante no existe en el repositorio.

    Se espera que el método:
    - Retorne Optional.empty().
    **/
    @Test
    public void checkAvailability_WhenVariantNotFound_ReturnsEmptyOptional() {
        //given
        when(repository.findByVariantId(999L)).thenReturn(Optional.empty());

        //when
        Optional<VariantAvailability> result = productService.checkAvailability(999L);

        //then
        assertThat(result).isEmpty();
        verify(repository).findByVariantId(999L);
    }

    /**
    En esta prueba unitaria se espera que se retorne disponible = false cuando
    la variante consultada tiene stock cero.

    Características extras:
    - La variante existe pero su stock es 0.
    - Se verifica el campo available y stockQuantity en la respuesta.

    Se espera que el método:
    - Retorne un Optional presente con la información.
    - available debe ser false.
    - stockQuantity debe ser 0.
    **/
    @Test
    public void checkAvailability_WhenZeroStock_ReturnsNotAvailable() {
        //given
        Product zeroStockProduct = testProduct.toBuilder()
                .variants(List.of(ProductVariant.builder()
                        .variantId(101L)
                        .sku("SKU-VAR-1")
                        .variantName("Red")
                        .price(BigDecimal.valueOf(100))
                        .currency("USD")
                        .stockQuantity(0)
                        .build()))
                .build();
        when(repository.findByVariantId(101L)).thenReturn(Optional.of(zeroStockProduct));

        //when
        Optional<VariantAvailability> result = productService.checkAvailability(101L);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().available()).isFalse();
        assertThat(result.get().stockQuantity()).isZero();
    }

    /**
    En esta prueba se valida el borrado lógico de un producto.
    
    Características extras:
    - El producto existe en el sistema
    - Se proporciona un motivo de borrado
    
    Se espera que el método:
    - Cambie el estado del producto a ARCHIVED
    - Guarde los cambios y publique eventos
    **/
    @Test
    public void delete_ExistingProduct_ChangesStatusToArchived() {
        //given
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(repository.save(any(Product.class))).thenReturn(testProduct);
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        //when
        productService.delete(new DeleteProductCommand(1L, "R"));

        //then
        verify(repository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().status()).as("El producto guardado debe tener estado ARCHIVED").isEqualTo(ProductStatus.ARCHIVED);
    }
}
