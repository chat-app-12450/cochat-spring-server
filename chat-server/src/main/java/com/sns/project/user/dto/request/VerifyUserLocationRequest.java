package com.sns.project.user.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyUserLocationRequest {

    @Size(max = 120, message = "locationLabel은 120자 이하여야 합니다.")
    private String locationLabel;

    @NotNull(message = "latitude는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "latitude는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "latitude는 90 이하여야 합니다.")
    private Double latitude;

    @NotNull(message = "longitude는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "longitude는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "longitude는 180 이하여야 합니다.")
    private Double longitude;
}
