package com.avodev.techstore.responses;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    Long id;
    String variantName;
    Long price;
    Integer stockQuantity;
    Long productId;
    String productName;
}
