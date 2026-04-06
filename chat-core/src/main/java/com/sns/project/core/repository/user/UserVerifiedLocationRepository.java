package com.sns.project.core.repository.user;

import com.sns.project.core.domain.user.UserVerifiedLocation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserVerifiedLocationRepository extends JpaRepository<UserVerifiedLocation, Long> {

    Optional<UserVerifiedLocation> findByUser_Id(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE user_verified_location
        SET location = CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
        WHERE id = :id
        """, nativeQuery = true)
    void updateLocation(@Param("id") Long id, @Param("latitude") double latitude, @Param("longitude") double longitude);
}
