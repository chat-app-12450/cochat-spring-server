package com.sns.project.core.domain.product;

import com.sns.project.core.domain.BaseTimeEntity;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.exception.conflict.ProductStateConflictException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_products_status_created_at", columnList = "status, created_at"),
        @Index(name = "idx_products_seller_created_at", columnList = "seller_id, created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne + @JoinColumn 쪽이 연관관계 주인이므로 seller_id FK 를 직접 갱신한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserved_buyer_id")
    private User reservedBuyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sold_buyer_id")
    private User soldBuyer;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    // 같은 상품 row 를 동시에 수정할 때 마지막 저장이 앞선 저장을 덮어쓰지 않도록 버전을 둔다.
    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductImage> images = new ArrayList<>();

    @Builder
    private Product(String title, String description, Long price, ProductStatus status) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status != null ? status : ProductStatus.ON_SALE;
    }

    public void assignSeller(User seller) {
        this.seller = seller;
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.assignProduct(this);
    }

    public void replaceImages(List<ProductImage> newImages) {
        images.clear();
        for (ProductImage image : newImages) {
            addImage(image);
        }
    }

    public void updateDetails(String title, String description, Long price) {
        this.title = title;
        this.description = description;
        this.price = price;
    }

    public void changeStatusBySeller(ProductStatus status) {
        if (status == null) {
            throw new ProductStateConflictException("상품 상태는 비워둘 수 없습니다.");
        }

        if (this.status == status) {
            return;
        }

        if (this.status == ProductStatus.SOLD) {
            throw new ProductStateConflictException("판매완료된 상품은 다른 상태로 되돌릴 수 없습니다.");
        }

        if (status == ProductStatus.RESERVED) {
            throw new ProductStateConflictException("예약중 처리는 구매자 예약 API를 통해서만 변경할 수 있습니다.");
        }

        if (status == ProductStatus.ON_SALE) {
            this.reservedBuyer = null;
            this.soldBuyer = null;
        }

        if (status == ProductStatus.SOLD && this.reservedBuyer != null) {
            this.soldBuyer = this.reservedBuyer;
        }

        this.status = status;
    }

    public void reserve(User buyer) {
        validateBuyer(buyer);

        if (this.status != ProductStatus.ON_SALE) {
            throw new ProductStateConflictException("판매중인 상품만 예약할 수 있습니다.");
        }

        this.status = ProductStatus.RESERVED;
        this.reservedBuyer = buyer;
        this.soldBuyer = null;
    }

    public void purchase(User buyer) {
        validateBuyer(buyer);

        if (this.status == ProductStatus.SOLD) {
            throw new ProductStateConflictException("이미 판매완료된 상품입니다.");
        }

        if (this.status == ProductStatus.RESERVED) {
            if (this.reservedBuyer == null) {
                throw new ProductStateConflictException("판매자가 예약중으로 표시한 상품은 바로 구매할 수 없습니다.");
            }
            if (!this.reservedBuyer.getId().equals(buyer.getId())) {
                throw new ProductStateConflictException("다른 사용자가 예약한 상품입니다.");
            }
        }

        this.status = ProductStatus.SOLD;
        this.reservedBuyer = buyer;
        this.soldBuyer = buyer;
    }

    public void cancelReservation(User buyer) {
        validateBuyer(buyer);

        if (this.status != ProductStatus.RESERVED || this.reservedBuyer == null) {
            throw new ProductStateConflictException("현재 취소할 예약이 없습니다.");
        }

        if (!this.reservedBuyer.getId().equals(buyer.getId())) {
            throw new ProductStateConflictException("본인이 예약한 상품만 취소할 수 있습니다.");
        }

        this.status = ProductStatus.ON_SALE;
        this.reservedBuyer = null;
        this.soldBuyer = null;
    }

    private void validateBuyer(User buyer) {
        if (buyer == null) {
            throw new ProductStateConflictException("구매자 정보가 필요합니다.");
        }
        if (this.seller != null && this.seller.getId().equals(buyer.getId())) {
            throw new ProductStateConflictException("본인 상품은 예약하거나 구매할 수 없습니다.");
        }
    }
}
