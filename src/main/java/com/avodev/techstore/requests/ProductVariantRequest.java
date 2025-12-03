package com.avodev.techstore.requests;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantRequest {
    Long productId;
    Long brandId;
    Long categoryId;
    BigDecimal minPrice;
    BigDecimal maxPrice;
    Boolean inStock;
    String keyword;
    Map<Long, List<String>> specFilters;
}
