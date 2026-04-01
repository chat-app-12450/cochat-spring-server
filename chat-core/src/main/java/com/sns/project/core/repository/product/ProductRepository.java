package com.sns.project.core.repository.product;

import com.sns.project.core.domain.product.Product;
import com.sns.project.core.domain.product.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

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
