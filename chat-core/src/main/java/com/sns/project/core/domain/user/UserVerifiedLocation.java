package com.sns.project.core.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_verified_location",
    indexes = {
        @Index(name = "idx_user_verified_location_verified_at", columnList = "verified_at")
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVerifiedLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "location_label", length = 120)
    private String locationLabel;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    public void verify(String locationLabel, Double latitude, Double longitude, LocalDateTime verifiedAt) {
        this.locationLabel = locationLabel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.verifiedAt = verifiedAt;
    }
}
