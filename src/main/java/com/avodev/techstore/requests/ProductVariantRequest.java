package com.avodev.techstore.requests;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantRequest {

    @NotNull(message = "PRODUCT_ID_REQUIRED")
    @Positive(message = "PRODUCT_ID_INVALID")
    Long productId;

    @NotBlank(message = "VARIANT_NAME_REQUIRED")
    String variantName;

    @NotNull(message = "PRICE_REQUIRED")
    @Positive(message = "PRICE_INVALID")
    Long price;

    @NotNull(message = "STOCK_QUANTITY_REQUIRED")
    @PositiveOrZero(message = "STOCK_QUANTITY_INVALID")
    Integer stockQuantity;

}
