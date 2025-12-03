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

        Long finalPrice = productVariant.getPrice();
        Discount discount = productVariant.getDiscount();
        StringBuilder discountText = new StringBuilder();

        if (discount != null && Boolean.TRUE.equals(discount.getActive())) {
            if (discount.getType() == DiscountType.PERCENT) {
                // Phần trăm giảm
                BigDecimal percent = discount.getValue();
                discountText.append("Giảm ")
                        .append(percent.stripTrailingZeros().toPlainString())
                        .append("%");

                // Tính tiền giảm: finalPrice * percent / 100
                long discountAmount = Math.round(finalPrice * percent.doubleValue() / 100);
                finalPrice -= discountAmount;

            } else if (discount.getType() == DiscountType.FIXED) {
                // Số tiền cố định giảm
                BigDecimal fixed = discount.getValue();
                discountText.append("Giảm ")
                        .append(fixed.setScale(0, RoundingMode.HALF_UP).toPlainString())
                        .append("₫");

                long discountAmount = fixed.setScale(0, RoundingMode.HALF_UP).longValue();
                finalPrice -= discountAmount;
            }
            if (finalPrice < 0) {
                finalPrice = 0L;
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
