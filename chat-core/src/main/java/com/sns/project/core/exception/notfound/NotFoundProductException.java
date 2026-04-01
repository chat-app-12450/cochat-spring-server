package com.sns.project.core.exception.notfound;

public class NotFoundProductException extends RuntimeException {
    public NotFoundProductException(Long productId) {
        super("상품을 찾을 수 없습니다. 상품 아이디: " + productId);
    }
}
