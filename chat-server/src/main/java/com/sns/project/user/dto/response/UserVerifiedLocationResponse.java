package com.sns.project.user.dto.response;

import com.sns.project.core.domain.user.UserVerifiedLocation;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserVerifiedLocationResponse {

    private Long id;
    private String locationLabel;
    private Double latitude;
    private Double longitude;
    private LocalDateTime verifiedAt;

    public static UserVerifiedLocationResponse from(UserVerifiedLocation verifiedLocation) {
        return UserVerifiedLocationResponse.builder()
            .id(verifiedLocation.getId())
            .locationLabel(verifiedLocation.getLocationLabel())
            .latitude(verifiedLocation.getLatitude())
            .longitude(verifiedLocation.getLongitude())
            .verifiedAt(verifiedLocation.getVerifiedAt())
            .build();
    }
}
