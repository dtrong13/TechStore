package com.avodev.techstore.responses;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantCardResponse {
    Long id;
    String variantName;
    Long price;
    Integer stockQuantity;
    String discountText;
    Long finalPrice;
    String imageUrl;
}
