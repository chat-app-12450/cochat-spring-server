package com.sns.project.product;

import com.sns.project.product.dto.request.CreateProductRequest;
import com.sns.project.product.dto.request.UpdateProductRequest;
import com.sns.project.product.dto.request.UpdateProductStatusRequest;
import com.sns.project.product.dto.response.ProductDetailResponse;
import com.sns.project.product.dto.response.ProductListResponse;
import com.sns.project.product.dto.response.ProductSummaryResponse;
import com.sns.project.core.domain.product.Product;
import com.sns.project.core.domain.product.ProductImage;
import com.sns.project.core.domain.product.ProductStatus;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.exception.forbidden.ForbiddenException;
import com.sns.project.core.exception.notfound.NotFoundProductException;
import com.sns.project.core.exception.notfound.NotFoundUserException;
import com.sns.project.core.repository.product.NearbyProductProjection;
import com.sns.project.core.repository.product.ProductRepository;
import com.sns.project.core.repository.user.UserRepository;
import com.sns.project.user.UserLocationVerificationService;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserLocationVerificationService userLocationVerificationService;

    @Transactional
    public ProductDetailResponse createProduct(Long sellerId, CreateProductRequest request) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new NotFoundUserException(String.valueOf(sellerId)));
        var verifiedLocation = userLocationVerificationService.getRequiredVerifiedLocation(sellerId);

        Product product = Product.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .price(request.getPrice())
            .status(ProductStatus.ON_SALE)
            .build();
        product.updateLocation(
            verifiedLocation.getLocationLabel(),
            verifiedLocation.getLatitude(),
            verifiedLocation.getLongitude()
        );

        seller.addProduct(product);
        product.replaceImages(toProductImages(request.getImageUrls()));

        Product savedProduct = productRepository.save(product);
        productRepository.updateLocation(
            savedProduct.getId(),
            verifiedLocation.getLatitude(),
            verifiedLocation.getLongitude()
        );
        return ProductDetailResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductListResponse getProducts(ProductStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Long> productIdPage = status == null
            ? productRepository.findPageIdsOrderByCreatedAtDesc(pageable)
            : productRepository.findPageIdsByStatusOrderByCreatedAtDesc(status, pageable);

        return buildProductListResponse(productIdPage, page, size);
    }

    @Transactional(readOnly = true)
    public ProductListResponse getNearbyProducts(Long userId, ProductStatus status, int page, int size, Double radiusKm) {
        var verifiedLocation = userLocationVerificationService.getRequiredVerifiedLocation(userId);
        double searchRadiusKm = radiusKm != null && radiusKm > 0 ? radiusKm : 3.0;
        double radiusMeters = searchRadiusKm * 1000.0;
        int offset = page * size;

        List<NearbyProductProjection> nearbyProducts = productRepository.findNearbyProducts(
            verifiedLocation.getLatitude(),
            verifiedLocation.getLongitude(),
            radiusMeters,
            status != null ? status.name() : null,
            size,
            offset
        );

        long totalCount = productRepository.countNearbyProducts(
            verifiedLocation.getLatitude(),
            verifiedLocation.getLongitude(),
            radiusMeters,
            status != null ? status.name() : null
        );

        return buildNearbyProductListResponse(nearbyProducts, totalCount, page, size);
    }

    @Transactional(readOnly = true)
    public ProductListResponse getMyProducts(Long sellerId, ProductStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Long> productIdPage = status == null
            ? productRepository.findPageIdsBySellerIdOrderByCreatedAtDesc(sellerId, pageable)
            : productRepository.findPageIdsBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, status, pageable);

        return buildProductListResponse(productIdPage, page, size);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long sellerId, Long productId, UpdateProductRequest request) {
        Product product = getOwnedProduct(productId, sellerId);
        product.updateDetails(request.getTitle(), request.getDescription(), request.getPrice());
        product.replaceImages(toProductImages(request.getImageUrls()));
        return ProductDetailResponse.from(product);
    }

    @Transactional
    public ProductDetailResponse updateProductStatus(Long sellerId, Long productId, UpdateProductStatusRequest request) {
        Product product = getOwnedProduct(productId, sellerId);
        product.changeStatusBySeller(request.getStatus());
        return ProductDetailResponse.from(product);
    }

    @Transactional
    public ProductDetailResponse reserveProduct(Long buyerId, Long productId) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new NotFoundProductException(productId));
        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new NotFoundUserException(String.valueOf(buyerId)));

        product.reserve(buyer);
        return ProductDetailResponse.from(product);
    }

    @Transactional
    public ProductDetailResponse purchaseProduct(Long buyerId, Long productId) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new NotFoundProductException(productId));
        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new NotFoundUserException(String.valueOf(buyerId)));

        product.purchase(buyer);
        return ProductDetailResponse.from(product);
    }

    @Transactional
    public ProductDetailResponse cancelReservation(Long buyerId, Long productId) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new NotFoundProductException(productId));
        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new NotFoundUserException(String.valueOf(buyerId)));

        product.cancelReservation(buyer);
        return ProductDetailResponse.from(product);
    }

    @Transactional
    public ProductDetailResponse reopenProduct(Long sellerId, Long productId) {
        Product product = getOwnedProduct(productId, sellerId);
        product.changeStatusBySeller(ProductStatus.ON_SALE);
        return ProductDetailResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long sellerId, Long productId) {
        Product product = getOwnedProduct(productId, sellerId);
        productRepository.delete(product);
    }

    private ProductListResponse buildProductListResponse(Page<Long> productIdPage, int page, int size) {

        List<Long> productIds = productIdPage.getContent();
        if (productIds.isEmpty()) {
            return ProductListResponse.builder()
                .page(page)
                .size(size)
                .count(0)
                .totalCount(productIdPage.getTotalElements())
                .totalPages(productIdPage.getTotalPages())
                .hasNext(productIdPage.hasNext())
                .hasPrevious(productIdPage.hasPrevious())
                .products(List.of())
                .build();
        }

        List<Product> products = productRepository.findAllWithSellerAndImagesByIdIn(productIds);
        Map<Long, Integer> order = new HashMap<>();
        for (int index = 0; index < productIds.size(); index++) {
            order.put(productIds.get(index), index);
        }
        products.sort(Comparator.comparingInt(product -> order.get(product.getId())));

        List<ProductSummaryResponse> responses = products.stream()
            .map(ProductSummaryResponse::from)
            .toList();

        return ProductListResponse.builder()
            .page(page)
            .size(size)
            .count(responses.size())
            .totalCount(productIdPage.getTotalElements())
            .totalPages(productIdPage.getTotalPages())
            .hasNext(productIdPage.hasNext())
            .hasPrevious(productIdPage.hasPrevious())
            .products(responses)
            .build();
    }

    private ProductListResponse buildNearbyProductListResponse(
        List<NearbyProductProjection> nearbyProducts,
        long totalCount,
        int page,
        int size
    ) {
        if (nearbyProducts.isEmpty()) {
            int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / size);
            return ProductListResponse.builder()
                .page(page)
                .size(size)
                .count(0)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .hasNext(page + 1 < totalPages)
                .hasPrevious(page > 0)
                .products(List.of())
                .build();
        }

        List<Long> productIds = nearbyProducts.stream()
            .map(NearbyProductProjection::getProductId)
            .toList();
        Map<Long, Integer> order = new HashMap<>();
        Map<Long, Double> distanceByProductId = new HashMap<>();
        for (int index = 0; index < nearbyProducts.size(); index++) {
            NearbyProductProjection projection = nearbyProducts.get(index);
            order.put(projection.getProductId(), index);
            distanceByProductId.put(projection.getProductId(), projection.getDistanceMeters());
        }

        List<Product> products = productRepository.findAllWithSellerAndImagesByIdIn(productIds);
        products.sort(Comparator.comparingInt(product -> order.get(product.getId())));

        List<ProductSummaryResponse> responses = products.stream()
            .map(product -> ProductSummaryResponse.from(product, distanceByProductId.get(product.getId())))
            .toList();

        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / size);
        return ProductListResponse.builder()
            .page(page)
            .size(size)
            .count(responses.size())
            .totalCount(totalCount)
            .totalPages(totalPages)
            .hasNext(page + 1 < totalPages)
            .hasPrevious(page > 0)
            .products(responses)
            .build();
    }

    private Product getOwnedProduct(Long productId, Long sellerId) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new NotFoundProductException(productId));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ForbiddenException("본인 상품만 수정하거나 삭제할 수 있습니다.");
        }
        return product;
    }

    private List<ProductImage> toProductImages(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }

        List<ProductImage> images = new java.util.ArrayList<>();
        for (int index = 0; index < imageUrls.size(); index++) {
            images.add(ProductImage.builder()
                .imageUrl(imageUrls.get(index))
                .sortOrder(index)
                .build());
        }
        return images;
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new NotFoundProductException(productId));
        return ProductDetailResponse.from(product);
    }
}
