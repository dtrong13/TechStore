package com.avodev.techstore.requests;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantSearchRequest {
    Long productId;
    Long brandId;
    Long categoryId;
    Long minPrice;
    Long maxPrice;
    Boolean inStock;
    String keyword;
    Map<Long, List<String>> specFilters;
}
