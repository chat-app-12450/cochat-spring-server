package com.sns.project.product.dto.request;

import com.sns.project.core.domain.product.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProductStatusRequest {

    @NotNull(message = "상품 상태는 필수입니다.")
    private ProductStatus status;
}
