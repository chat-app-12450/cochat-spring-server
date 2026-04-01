package com.sns.project.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProductRequest {

    @NotBlank(message = "상품 제목은 비어 있을 수 없습니다.")
    @Size(max = 100, message = "상품 제목은 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "상품 설명은 비어 있을 수 없습니다.")
    @Size(max = 2000, message = "상품 설명은 2000자 이하여야 합니다.")
    private String description;

    @NotNull(message = "상품 가격은 필수입니다.")
    @PositiveOrZero(message = "상품 가격은 0 이상이어야 합니다.")
    private Long price;

    @Size(max = 5, message = "상품 이미지는 최대 5장까지 등록할 수 있습니다.")
    private List<
        @NotBlank(message = "이미지 URL은 비어 있을 수 없습니다.")
        @Size(max = 2048, message = "이미지 URL은 2048자 이하여야 합니다.")
        String> imageUrls;
}
