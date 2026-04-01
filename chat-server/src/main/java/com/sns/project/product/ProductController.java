package com.sns.project.product;

import com.sns.project.auth.AuthRequired;
import com.sns.project.auth.UserContext;
import com.sns.project.chat.controller.dto.response.RoomInfoResponse;
import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.product.dto.request.CreateProductRequest;
import com.sns.project.product.dto.request.UpdateProductRequest;
import com.sns.project.product.dto.request.UpdateProductStatusRequest;
import com.sns.project.product.dto.response.ProductDetailResponse;
import com.sns.project.product.dto.response.ProductListResponse;
import com.sns.project.core.domain.product.ProductStatus;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product", description = "중고거래 상품 API")
public class ProductController {

    private final ProductService productService;
    private final ChatRoomService chatRoomService;

    @PostMapping
    @AuthRequired
    @Operation(summary = "상품 등록", description = "로그인한 사용자가 판매 상품을 등록합니다.")
    public ApiResult<ProductDetailResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Long sellerId = UserContext.getUserId();
        return ApiResult.success(productService.createProduct(sellerId, request));
    }

    @PostMapping("/{productId}/chat-rooms")
    @AuthRequired
    @Operation(summary = "상품 기반 1:1 채팅방 생성", description = "상품 판매자와 현재 사용자 사이의 1:1 채팅방을 생성하거나 기존 방을 반환합니다.")
    public ApiResult<RoomInfoResponse> createProductChatRoom(
        @PathVariable @Positive(message = "productId는 1 이상이어야 합니다.") Long productId) {
        Long buyerId = UserContext.getUserId();
        return ApiResult.success(chatRoomService.createProductRoom(productId, buyerId));
    }

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "판매 상태별 상품 목록을 최신순으로 조회합니다.")
    public ApiResult<ProductListResponse> getProducts(
        @RequestParam(name = "status", required = false) ProductStatus status,
        @RequestParam(name = "page", defaultValue = "0") @PositiveOrZero(message = "page는 0 이상이어야 합니다.") int page,
        @RequestParam(name = "size", defaultValue = "20") @Positive(message = "size는 1 이상이어야 합니다.")
        @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return ApiResult.success(productService.getProducts(status, page, size));
    }

    @GetMapping("/me")
    @AuthRequired
    @Operation(summary = "내 상품 목록 조회", description = "로그인한 사용자가 등록한 상품 목록을 조회합니다.")
    public ApiResult<ProductListResponse> getMyProducts(
        @RequestParam(name = "status", required = false) ProductStatus status,
        @RequestParam(name = "page", defaultValue = "0") @PositiveOrZero(message = "page는 0 이상이어야 합니다.") int page,
        @RequestParam(name = "size", defaultValue = "20") @Positive(message = "size는 1 이상이어야 합니다.")
        @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        Long sellerId = UserContext.getUserId();
        return ApiResult.success(productService.getMyProducts(sellerId, status, page, size));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보와 판매자 정보를 조회합니다.")
    public ApiResult<ProductDetailResponse> getProduct(
        @PathVariable @Positive(message = "productId는 1 이상이어야 합니다.") Long productId) {
        return ApiResult.success(productService.getProduct(productId));
    }

    @PatchMapping("/{productId}")
    @AuthRequired
    @Operation(summary = "상품 수정", description = "로그인한 판매자가 본인 상품 정보를 수정합니다.")
    public ApiResult<ProductDetailResponse> updateProduct(
        @PathVariable @Positive(message = "productId는 1 이상이어야 합니다.") Long productId,
        @Valid @RequestBody UpdateProductRequest request) {
        Long sellerId = UserContext.getUserId();
        return ApiResult.success(productService.updateProduct(sellerId, productId, request));
    }

    @PatchMapping("/{productId}/status")
    @AuthRequired
    @Operation(summary = "상품 상태 변경", description = "로그인한 판매자가 본인 상품 상태를 변경합니다.")
    public ApiResult<ProductDetailResponse> updateProductStatus(
        @PathVariable @Positive(message = "productId는 1 이상이어야 합니다.") Long productId,
        @Valid @RequestBody UpdateProductStatusRequest request) {
        Long sellerId = UserContext.getUserId();
        return ApiResult.success(productService.updateProductStatus(sellerId, productId, request));
    }

    @PostMapping("/{productId}/reserve")
    @AuthRequired
    @Operation(summary = "상품 예약", description = "로그인한 사용자가 판매중인 상품을 예약합니다.")
    public ApiResult<ProductDetailResponse> reserveProduct(
        @PathVariable @Positive(message = "productId는 1 이상이어야 합니다.") Long productId) {
        Long buyerId = UserContext.getUserId();
        return ApiResult.success(productService.reserveProduct(buyerId, productId));
    }

    @PostMapping("/{productId}/purchase")
    @AuthRequired
    @Operation(summary = "상품 구매", description = "로그인한 사용자가 판매중이거나 본인이 예약한 상품을 구매 확정합니다.")
    public ApiResult<ProductDetailResponse> purchaseProduct(
        @PathVariable @Positive(message = "productId는 1 이상이어야 합니다.") Long productId) {
        Long buyerId = UserContext.getUserId();
        return ApiResult.success(productService.purchaseProduct(buyerId, productId));
    }

    @PostMapping("/{productId}/reserve/cancel")
    @AuthRequired
    @Operation(summary = "예약 취소", description = "로그인한 사용자가 본인이 예약한 상품 예약을 취소하고 다시 판매중으로 돌립니다.")
    public ApiResult<ProductDetailResponse> cancelReservation(
        @PathVariable @Positive(message = "productId는 1 이상이어야 합니다.") Long productId) {
        Long buyerId = UserContext.getUserId();
        return ApiResult.success(productService.cancelReservation(buyerId, productId));
    }

    @PostMapping("/{productId}/reopen")
    @AuthRequired
    @Operation(summary = "판매중 복귀", description = "로그인한 판매자가 본인 상품을 다시 판매중 상태로 되돌립니다.")
    public ApiResult<ProductDetailResponse> reopenProduct(
        @PathVariable @Positive(message = "productId는 1 이상이어야 합니다.") Long productId) {
        Long sellerId = UserContext.getUserId();
        return ApiResult.success(productService.reopenProduct(sellerId, productId));
    }

    @DeleteMapping("/{productId}")
    @AuthRequired
    @Operation(summary = "상품 삭제", description = "로그인한 판매자가 본인 상품을 삭제합니다.")
    public ApiResult<String> deleteProduct(
        @PathVariable @Positive(message = "productId는 1 이상이어야 합니다.") Long productId) {
        Long sellerId = UserContext.getUserId();
        productService.deleteProduct(sellerId, productId);
        return ApiResult.success("상품이 삭제되었습니다.");
    }
}
