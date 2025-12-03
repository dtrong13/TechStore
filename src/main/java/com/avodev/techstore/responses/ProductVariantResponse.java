package com.avodev.techstore.responses;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    Long id;
    String variantName;
    BigDecimal price;
    Integer stockQuantity;
    String discountText;
    BigDecimal finalPrice;
    String imageUrl;
}
