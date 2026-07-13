package com.ecommerce.catalog.infrastructure.adapter.in.web.mapper;

import com.ecommerce.catalog.application.dto.CreateProductCommand;
import com.ecommerce.catalog.application.dto.ImageRequest;
import com.ecommerce.catalog.application.dto.VariantRequest;
import com.ecommerce.catalog.domain.model.PagedResult;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductImage;
import com.ecommerce.catalog.domain.model.ProductVariant;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.PagedResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ProductCreateRequest;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente encargado de aislar la capa web de la capa de aplicación/dominio.
 * Mapea DTOs HTTP hacia Comandos de Caso de Uso, y Records de Dominio hacia DTOs HTTP.
 */
@Component
public class ProductWebMapper {

    public CreateProductCommand toCommand(ProductCreateRequest request) {
        if (request == null) return null;

        List<VariantRequest> variants = request.variants() == null ? List.of() :
                request.variants().stream()
                .map(v -> new VariantRequest(
                        v.sku(),
                        v.variantName(),
                        v.price(),
                        v.currency(),
                        v.stockQuantity(),
                        v.attributes()
                )).collect(Collectors.toList());

        List<ImageRequest> images = request.images() == null ? List.of() :
                request.images().stream()
                .map(i -> new ImageRequest(
                        i.url(),
                        i.altText(),
                        i.sortOrder()
                )).collect(Collectors.toList());

        return new CreateProductCommand(
                request.skuBase(),
                request.name(),
                request.description(),
                request.basePrice(),
                request.currency(),
                request.categories(),
                request.tags(),
                request.attributes(),
                variants,
                images
        );
    }

    public ProductResponse toResponse(Product domain) {
        if (domain == null) return null;

        List<ProductResponse.VariantResponse> variants = domain.variants() == null ? List.of() :
                domain.variants().stream()
                .map(this::toVariantResponse)
                .collect(Collectors.toList());

        List<ProductResponse.ImageResponse> images = domain.images() == null ? List.of() :
                domain.images().stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());

        return new ProductResponse(
                String.valueOf(domain.id()),
                domain.skuBase(),
                domain.name(),
                domain.slug(),
                domain.description(),
                domain.basePrice(),
                domain.currency(),
                domain.categories(),
                domain.tags(),
                domain.attributes(),
                domain.status() != null ? domain.status().name() : null,
                variants,
                images,
                domain.createdAt(),
                domain.updatedAt()
        );
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        if (products == null) return List.of();
        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    public PagedResponse<ProductResponse> toPagedResponse(PagedResult<Product> pagedResult) {
        if (pagedResult == null) return null;
        List<ProductResponse> responses = toResponseList(pagedResult.content());
        boolean last = pagedResult.totalPages() <= 0 || pagedResult.page() >= pagedResult.totalPages() - 1;
        return new PagedResponse<>(
                responses,
                pagedResult.page(),
                pagedResult.size(),
                pagedResult.totalElements(),
                pagedResult.totalPages(),
                last
        );
    }

    private ProductResponse.VariantResponse toVariantResponse(ProductVariant domain) {
        return new ProductResponse.VariantResponse(
                String.valueOf(domain.variantId()),
                domain.sku(),
                domain.variantName(),
                domain.price(),
                domain.currency(),
                domain.stockQuantity(),
                domain.attributes()
        );
    }

    private ProductResponse.ImageResponse toImageResponse(ProductImage domain) {
        return new ProductResponse.ImageResponse(
                domain.url(),
                domain.altText(),
                domain.sortOrder()
        );
    }
}
