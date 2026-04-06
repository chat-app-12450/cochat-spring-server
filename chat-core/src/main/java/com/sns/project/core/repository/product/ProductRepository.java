package com.sns.project.core.repository.product;

import com.sns.project.core.domain.product.Product;
import com.sns.project.core.domain.product.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE products
        SET location = CASE
          WHEN :latitude IS NULL OR :longitude IS NULL THEN NULL
          ELSE CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
        END
        WHERE id = :productId
        """, nativeQuery = true)
    void updateLocation(
        @Param("productId") Long productId,
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude);

    // nearby 상품 검색용 native query
    // 1) 사용자 인증 위치를 1행짜리 geography point 로 만든다.
    // 2) products.location geography 컬럼과 사용자 위치를 직접 비교한다.
    // 3) ST_DWithin 으로 반경 안의 상품만 남긴다.
    // 4) ST_Distance 로 실제 거리를 계산해 가까운 순으로 정렬한다.
    @Query(value = """
        -- 사용자 인증 위치를 geography point 로 만든다.
        WITH user_location AS (
          SELECT CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography) AS point
        )
        SELECT
          p.id AS productId,
          -- 사용자 위치와 각 상품 사이의 실제 거리를 meters 로 계산한다.
          ST_Distance(p.location, ul.point) AS distanceMeters
        FROM products p
        -- user_location 은 1행짜리라서, 모든 상품과 "사용자 위치 1개"를 비교하기 위해 CROSS JOIN 한다.
        CROSS JOIN user_location ul
        WHERE p.location IS NOT NULL
          -- 상품 위치와 사용자 위치가 radiusMeters 반경 안에 있는 상품만 남긴다.
          AND ST_DWithin(p.location, ul.point, :radiusMeters)
          AND (:status IS NULL OR p.status = :status)
        -- 가까운 상품부터 보여주고, 동일 거리면 id 역순으로 고정 정렬한다.
        ORDER BY distanceMeters ASC, p.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<NearbyProductProjection> findNearbyProducts(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeters") double radiusMeters,
        @Param("status") String status,
        @Param("limit") int limit,
        @Param("offset") long offset);

    @Query(value = """
        WITH user_location AS (
          SELECT CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography) AS point
        )
        SELECT COUNT(*)
        FROM products p
        CROSS JOIN user_location ul
        WHERE p.location IS NOT NULL
          AND ST_DWithin(p.location, ul.point, :radiusMeters)
          AND (:status IS NULL OR p.status = :status)
        """, nativeQuery = true)
    long countNearbyProducts(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeters") double radiusMeters,
        @Param("status") String status);

    @Query("""
        select p.id
        from Product p
        order by p.createdAt desc
        """)
    Page<Long> findPageIdsOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        select p.id
        from Product p
        where p.status = :status
        order by p.createdAt desc
        """)
    Page<Long> findPageIdsByStatusOrderByCreatedAtDesc(@Param("status") ProductStatus status, Pageable pageable);

    @Query("""
        select p.id
        from Product p
        where p.seller.id = :sellerId
        order by p.createdAt desc
        """)
    Page<Long> findPageIdsBySellerIdOrderByCreatedAtDesc(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("""
        select p.id
        from Product p
        where p.seller.id = :sellerId
          and p.status = :status
        order by p.createdAt desc
        """)
    Page<Long> findPageIdsBySellerIdAndStatusOrderByCreatedAtDesc(
        @Param("sellerId") Long sellerId,
        @Param("status") ProductStatus status,
        Pageable pageable);

    @Query("""
        select distinct p
        from Product p
        join fetch p.seller
        left join fetch p.images
        where p.id in :productIds
        """)
    List<Product> findAllWithSellerAndImagesByIdIn(@Param("productIds") List<Long> productIds);

    @Query("""
        select distinct p
        from Product p
        join fetch p.seller
        left join fetch p.reservedBuyer
        left join fetch p.soldBuyer
        where p.id = :productId
        """)
    Optional<Product> findByIdWithSeller(@Param("productId") Long productId);

    @Query("""
        select distinct p
        from Product p
        join fetch p.seller
        left join fetch p.reservedBuyer
        left join fetch p.soldBuyer
        left join fetch p.images
        where p.id = :productId
        """)
    Optional<Product> findDetailById(@Param("productId") Long productId);
}
