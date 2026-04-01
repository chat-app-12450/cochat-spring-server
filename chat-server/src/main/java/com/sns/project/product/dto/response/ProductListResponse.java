package com.sns.project.product.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductListResponse {

    private int page;
    private int size;
    private int count;
    private long totalCount;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private List<ProductSummaryResponse> products;
}
