package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb;

import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document.ProductDocument;
import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.mapper.ProductPersistenceMapper;
import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.repository.SpringDataMongoProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPersistenceAdapterTest {

    @Mock
    private SpringDataMongoProductRepository mongoRepository;

    @Mock
    private ProductPersistenceMapper mapper;

    @InjectMocks
    private ProductPersistenceAdapter adapter;

    /**
    En esta prueba unitaria se valida el guardado delegando al repositorio Mongo.
    
    Características extras:
    - El producto no existe previamente en la base de datos.
    - Se verifica la conversión entre entidad y documento.
    
    Se espera que el método:
    - Persista el documento mediante `mongoRepository.save`.
    - Devuelva el objeto `Product` sin modificaciones.
    **/
    @Test
    void save_WhenCalled_ShouldDelegateToMongoRepository() {
        //given
        Product product = Product.builder()
                .id(1L)
                .skuBase("SKU")
                .name("Name")
                .slug("name")
                .status(ProductStatus.DRAFT)
                .build();
        ProductDocument document = new ProductDocument();
        document.setId(1L);
        when(mapper.toDocument(product)).thenReturn(document);
        when(mongoRepository.findById(1L)).thenReturn(Optional.empty());
        when(mongoRepository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(product);

        //when
        Product result = adapter.save(product);

        //then
        assertThat(result).isEqualTo(product);
        verify(mongoRepository).save(document);
    }

    /**
    En esta prueba unitaria se valida la búsqueda de un producto por ID mediante el adaptador.

    Características extras:
    - El documento con el ID solicitado está presente en la base de datos.
    - Se verifica la correcta conversión del documento a la entidad dominio.

    Se espera que el método:
    - Consulte el repositorio con `mongoRepository.findById`.
    - Devuelva un `Optional<Product>` que contenga el producto esperado.
    **/
    @Test
    void findById_WhenExists_ShouldReturnProduct() {
        //given
        ProductDocument document = new ProductDocument();
        document.setId(2L);
        Product product = Product.builder().id(2L).skuBase("S").name("N").slug("n").status(ProductStatus.ACTIVE).build();
        when(mongoRepository.findById(2L)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(product);

        //when
        Optional<Product> result = adapter.findById(2L);

        //then
        assertThat(result).contains(product);
    }

    /**
    En esta prueba unitaria se valida findAll de forma paginada mapeando los documentos recuperados.

    Características extras:
    - Se retorna una página con un único documento.
    - Se verifica que el mapeo a dominio sea correcto.

    Se espera que el método:
    - Consulte el repositorio mediante `mongoRepository.findAll(Pageable)`.
    - Convierta cada documento a una entidad `Product`.
    - Devuelva un `PagedResult` conteniendo el producto esperado y los metadatos de paginación.
    **/
    @Test
    @SuppressWarnings("unchecked")
    void findAll_WhenDocumentsExist_ShouldReturnPagedResult() {
        //given
        ProductDocument document = new ProductDocument();
        Product product = Product.builder().id(3L).skuBase("S").name("N").slug("n").status(ProductStatus.DRAFT).build();
        org.springframework.data.domain.Page<ProductDocument> page = org.mockito.Mockito.mock(org.springframework.data.domain.Page.class);
        
        when(page.getContent()).thenReturn(List.of(document));
        when(page.getNumber()).thenReturn(0);
        when(page.getSize()).thenReturn(20);
        when(page.getTotalElements()).thenReturn(1L);
        when(page.getTotalPages()).thenReturn(1);
        
        when(mongoRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        when(mapper.toDomain(document)).thenReturn(product);

        //when
        com.ecommerce.catalog.domain.model.PagedResult<Product> results = adapter.findAll(0, 20);

        //then
        assertThat(results.content()).containsExactly(product);
        assertThat(results.page()).isEqualTo(0);
        assertThat(results.size()).isEqualTo(20);
        assertThat(results.totalElements()).isEqualTo(1L);
        assertThat(results.totalPages()).isEqualTo(1);
    }

    /**
    En esta prueba unitaria se valida existsBySku delegando al repositorio.

    Características extras:
    - Verifica que el repositorio devuelve true cuando el SKU está presente.
    - No se realizan operaciones de escritura.

    Se espera que el método:
    - Consulte el repositorio mediante `mongoRepository.existsBySkuBase`.
    - Devuelva true indicando la existencia del producto.
    **/
    @Test
    void existsBySku_WhenSkuPresent_ShouldReturnTrue() {
        //given
        when(mongoRepository.existsBySkuBase("SKU-X")).thenReturn(true);

        //when
        boolean exists = adapter.existsBySku("SKU-X");

        //then
        assertThat(exists).isTrue();
    }

    /**
    En esta prueba unitaria se valida que el método deleteById delega correctamente al repositorio.

    Características extras:
    - No hay retorno, solo interacción con el repositorio.
    - Se verifica la llamada al método `mongoRepository.deleteById` con el ID correcto.

    Se espera que el método:
    - Consulte el repositorio mediante `mongoRepository.deleteById`.
    - No devuelva valor.
    **/
    @Test
    void deleteById_WhenCalled_ShouldInvokeRepository() {
        //when
        adapter.deleteById(9L);

        //then
        verify(mongoRepository).deleteById(9L);
    }

    /**
    En esta prueba unitaria se valida la búsqueda de un producto por slug mediante el adaptador.
    
    Características extras:
    - Se proporciona un slug existente.
    - El repositorio devuelve un documento opcional.
    - Se mapea el documento a la entidad dominio.
    
    Se espera que el método:
    - Consulte el repositorio con `mongoRepository.findBySlug`.
    - Convierta el `ProductDocument` a `Product`.
    - Devuelva un `Optional<Product>` que contenga el producto esperado.
    **/
    @Test
    void findBySlug_WhenExists_ShouldReturnProduct() {
        //given
        ProductDocument document = new ProductDocument();
        Product product = Product.builder().id(4L).skuBase("S").name("N").slug("my-slug").status(ProductStatus.DRAFT).build();
        when(mongoRepository.findBySlug("my-slug")).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(product);

        //when
        Optional<Product> result = adapter.findBySlug("my-slug");

        //then
        assertThat(result).contains(product);
    }

    /**
    En esta prueba unitaria se valida la búsqueda de un producto por variantId mediante el adaptador.

    Características extras:
    - Se proporciona un variantId existente.
    - El repositorio devuelve un documento opcional.
    - Se mapea el documento a la entidad dominio.

    Se espera que el método:
    - Consulte el repositorio con `mongoRepository.findByVariantsVariantId`.
    - Convierta el `ProductDocument` a `Product`.
    - Devuelva un `Optional<Product>` que contenga el producto esperado.
    **/
    @Test
    void findByVariantId_WhenExists_ShouldReturnProduct() {
        //given
        ProductDocument document = new ProductDocument();
        Product product = Product.builder().id(5L).skuBase("S").name("N").slug("slug").status(ProductStatus.DRAFT).build();
        when(mongoRepository.findByVariantsVariantId(77L)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(product);

        //when
        Optional<Product> result = adapter.findByVariantId(77L);

        //then
        assertThat(result).contains(product);
    }

    /**
    En esta prueba unitaria se valida que al actualizar se preserve la versión del documento existente.

    Características extras:
    - Se simula la existencia de un documento previo con versión establecida.
    - Se verifica que la versión del documento no sea sobrescrita al guardar.

    Se espera que el método:
    - Consulte el repositorio mediante `mongoRepository.findById` para obtener la versión actual.
    - Asigne la versión existente al documento que se persiste.
    - Mantenga la versión del documento después de la operación `save`.
    **/
    @Test
    void save_WhenProductExists_ShouldPreserveVersion() {
        //given
        Product product = Product.builder().id(6L).skuBase("S").name("N").slug("slug").status(ProductStatus.DRAFT).build();
        ProductDocument document = new ProductDocument();
        document.setId(6L);
        ProductDocument existing = new ProductDocument();
        existing.setId(6L);
        existing.setVersion(3L);
        when(mapper.toDocument(product)).thenReturn(document);
        when(mongoRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(mongoRepository.save(any())).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(product);

        //when
        adapter.save(product);

        //then
        assertThat(document.getVersion()).isEqualTo(3L);
    }
}
