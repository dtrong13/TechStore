package com.avodev.techstore.responses;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantDetailResponse {
    Long id;
    String variantName;
    Long price;
    Integer stockQuantity;
    String discountText;
    Long finalPrice;
    List<String> images;
    List<VariantSpecResponse> variantSpecs;
}
