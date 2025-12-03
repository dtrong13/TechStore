package com.avodev.techstore.mappers;

import com.avodev.techstore.entities.Discount;
import com.avodev.techstore.entities.ProductVariant;
import com.avodev.techstore.entities.VariantImage;
import com.avodev.techstore.enums.DiscountType;
import com.avodev.techstore.responses.ProductVariantResponse;
import com.avodev.techstore.services.VariantImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Component
@RequiredArgsConstructor
public class ProductVariantMapper {
    private final VariantImageService variantImageService;

    public ProductVariantResponse toProductVariantResponse(ProductVariant productVariant) {
        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(productVariant.getId());
        response.setVariantName(productVariant.getVariantName());
        response.setPrice(productVariant.getPrice());
        response.setStockQuantity(productVariant.getStockQuantity());

        BigDecimal finalPrice = productVariant.getPrice();
        Discount discount = productVariant.getDiscount();
        StringBuilder discountText = new StringBuilder();

        if (discount != null && Boolean.TRUE.equals(discount.getActive())) {
            if (discount.getType() == DiscountType.PERCENT) {
                discountText.append("Giảm ")
                        .append(discount.getValue().intValue())
                        .append("%");
                finalPrice = finalPrice.subtract(finalPrice.multiply(discount.getValue().divide(new BigDecimal(100))));
            } else if (discount.getType() == DiscountType.FIXED) {
                discountText.append("Giảm ")
                        .append(discount.getValue().setScale(0, RoundingMode.HALF_UP))
                        .append("₫");
                finalPrice = finalPrice.subtract(discount.getValue());
            }
        }

        response.setFinalPrice(finalPrice);
        response.setDiscountText(discountText.toString());

        VariantImage image = variantImageService.findPrimaryByVariantId(productVariant.getId());
        if (image != null) {
            response.setImageUrl(image.getImageUrl());
        }

        return response;
    }
}
