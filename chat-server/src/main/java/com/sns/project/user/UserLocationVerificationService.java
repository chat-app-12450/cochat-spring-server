package com.sns.project.user;

import com.sns.project.core.domain.user.User;
import com.sns.project.core.domain.user.UserVerifiedLocation;
import com.sns.project.core.exception.badRequest.LocationVerificationRequiredException;
import com.sns.project.core.exception.notfound.NotFoundUserException;
import com.sns.project.core.repository.user.UserRepository;
import com.sns.project.core.repository.user.UserVerifiedLocationRepository;
import com.sns.project.user.dto.request.VerifyUserLocationRequest;
import com.sns.project.user.dto.response.UserVerifiedLocationResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLocationVerificationService {

    private final UserRepository userRepository;
    private final UserVerifiedLocationRepository userVerifiedLocationRepository;

    @Transactional
    public UserVerifiedLocationResponse verifyLocation(Long userId, VerifyUserLocationRequest request) {
        return UserVerifiedLocationResponse.from(
            verifyLocation(userId, normalizeLocationLabel(request.getLocationLabel()), request.getLatitude(), request.getLongitude())
        );
    }

    @Transactional
    public UserVerifiedLocation verifyLocation(Long userId, String locationLabel, double latitude, double longitude) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException(String.valueOf(userId)));

        LocalDateTime verifiedAt = LocalDateTime.now();
        UserVerifiedLocation verifiedLocation = userVerifiedLocationRepository.findByUser_Id(userId)
            .map(existing -> {
                existing.verify(locationLabel, latitude, longitude, verifiedAt);
                return existing;
            })
            .orElseGet(() -> userVerifiedLocationRepository.save(
                UserVerifiedLocation.builder()
                    .user(user)
                    .locationLabel(locationLabel)
                    .latitude(latitude)
                    .longitude(longitude)
                    .verifiedAt(verifiedAt)
                    .build()
            ));

        userVerifiedLocationRepository.updateLocation(verifiedLocation.getId(), latitude, longitude);
        return verifiedLocation;
    }

    @Transactional(readOnly = true)
    public UserVerifiedLocationResponse getCurrentVerifiedLocation(Long userId) {
        return userVerifiedLocationRepository.findByUser_Id(userId)
            .map(UserVerifiedLocationResponse::from)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public UserVerifiedLocation getRequiredVerifiedLocation(Long userId) {
        return userVerifiedLocationRepository.findByUser_Id(userId)
            .orElseThrow(() -> new LocationVerificationRequiredException("위치 인증 후 다시 시도해주세요."));
    }

    private String normalizeLocationLabel(String locationLabel) {
        if (locationLabel == null) {
            return null;
        }
        String trimmed = locationLabel.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
