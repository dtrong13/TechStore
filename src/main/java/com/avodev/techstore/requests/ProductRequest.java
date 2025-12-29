package com.avodev.techstore.requests;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    @NotBlank(message = "PRODUCT_NAME_REQUIRED")
    String name;

    String description;

    @NotNull(message = "CATEGORY_ID_REQUIRED")
    @Positive(message = "CATEGORY_ID_INVALID")
    Long categoryId;

    @NotNull(message = "BRAND_ID_REQUIRED")
    @Positive(message = "BRAND_ID_INVALID")
    Long brandId;
}
