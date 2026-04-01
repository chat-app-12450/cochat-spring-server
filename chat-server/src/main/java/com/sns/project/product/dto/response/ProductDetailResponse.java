package com.sns.project.product.dto.response;

import com.sns.project.core.domain.product.Product;
import com.sns.project.core.domain.product.ProductStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductDetailResponse {

    private Long id;
    private String title;
    private String description;
    private Long price;
    private ProductStatus status;
    private Long version;
    private String sellerUserId;
    private String sellerName;
    private String sellerProfileImageUrl;
    private String reservedBuyerUserId;
    private String reservedBuyerName;
    private String soldBuyerUserId;
    private String soldBuyerName;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductDetailResponse from(Product product) {
        return ProductDetailResponse.builder()
            .id(product.getId())
            .title(product.getTitle())
            .description(product.getDescription())
            .price(product.getPrice())
            .status(product.getStatus())
            .version(product.getVersion())
            .sellerUserId(product.getSeller().getUserId())
            .sellerName(product.getSeller().getName())
            .sellerProfileImageUrl(product.getSeller().getProfile_image_url())
            .reservedBuyerUserId(product.getReservedBuyer() != null ? product.getReservedBuyer().getUserId() : null)
            .reservedBuyerName(product.getReservedBuyer() != null ? product.getReservedBuyer().getName() : null)
            .soldBuyerUserId(product.getSoldBuyer() != null ? product.getSoldBuyer().getUserId() : null)
            .soldBuyerName(product.getSoldBuyer() != null ? product.getSoldBuyer().getName() : null)
            .imageUrls(product.getImages().stream().map(image -> image.getImageUrl()).toList())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}
