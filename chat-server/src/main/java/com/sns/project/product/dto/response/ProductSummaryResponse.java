package com.sns.project.product.dto.response;

import com.sns.project.core.domain.product.Product;
import com.sns.project.core.domain.product.ProductStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSummaryResponse {

    private Long id;
    private String title;
    private Long price;
    private ProductStatus status;
    private String thumbnailUrl;
    private String sellerUserId;
    private String sellerName;
    private LocalDateTime createdAt;

    public static ProductSummaryResponse from(Product product) {
        String thumbnailUrl = product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl();
        return ProductSummaryResponse.builder()
            .id(product.getId())
            .title(product.getTitle())
            .price(product.getPrice())
            .status(product.getStatus())
            .thumbnailUrl(thumbnailUrl)
            .sellerUserId(product.getSeller().getUserId())
            .sellerName(product.getSeller().getName())
            .createdAt(product.getCreatedAt())
            .build();
    }
}
