package com.avodev.techstore.services;


import com.avodev.techstore.dtos.Pagination;
import com.avodev.techstore.dtos.SortField;
import com.avodev.techstore.entities.Discount;
import com.avodev.techstore.entities.Product;
import com.avodev.techstore.entities.ProductVariant;
import com.avodev.techstore.entities.VariantImage;
import com.avodev.techstore.enums.DiscountType;
import com.avodev.techstore.enums.SortDirection;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.mappers.ProductVariantMapper;
import com.avodev.techstore.repositories.ProductRepository;
import com.avodev.techstore.repositories.ProductVariantRepository;
import com.avodev.techstore.repositories.VariantImageRepository;
import com.avodev.techstore.repositories.VariantSpecificationRepository;
import com.avodev.techstore.requests.ProductVariantRequest;
import com.avodev.techstore.requests.ProductVariantSearchRequest;
import com.avodev.techstore.requests.SearchRequest;
import com.avodev.techstore.responses.*;
import com.avodev.techstore.specifications.ProductVariantSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductVariantService {

    ProductRepository productRepository;
    ProductVariantRepository productVariantRepository;
    ProductVariantMapper productVariantMapper;
    VariantImageRepository variantImageRepository;
    VariantSpecificationRepository variantSpecificationRepository;

    public PageableResponse<ProductVariantCardResponse> searchProductVariants(SearchRequest<ProductVariantSearchRequest> request) {
        ProductVariantSearchRequest filter = request.getFilter();
        Pagination pagination = request.getPagination();
        List<SortField> sorts = request.getSorts();

        int pageNumber = Optional.ofNullable(pagination)
                .map(Pagination::getPageNumer)
                .map(p -> Math.max(p, 0))
                .orElse(0);

        int pageSize = Optional.ofNullable(pagination)
                .map(Pagination::getPageSize)
                .map(s -> s == -1 ? 100 : Math.max(1, Math.min(s, 100)))
                .orElse(20);
        Pageable pageable;
        if (sorts != null && !sorts.isEmpty()) {
            List<Sort.Order> orders = sorts.stream()
                    .map(sortField -> {
                        Sort.Direction direction = sortField.getDirection() == SortDirection.ASC
                                ? Sort.Direction.ASC : Sort.Direction.DESC;
                        String fieldName = mapSortFieldToColumn(sortField.getField());
                        return new Sort.Order(direction, fieldName);
                    })
                    .toList();
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by(orders));
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }

        Specification<ProductVariant> spec = (root, query, cb) -> cb.conjunction();

        spec = spec
                .and(ProductVariantSpecification.hasProductId(filter.getProductId()))
                .and(ProductVariantSpecification.hasBrandId(filter.getBrandId()))
                .and(ProductVariantSpecification.hasCategoryId(filter.getCategoryId()))
                .and(ProductVariantSpecification.priceBetween(filter.getMinPrice(), filter.getMaxPrice()))
                .and(ProductVariantSpecification.inStock(filter.getInStock()))
                .and(ProductVariantSpecification.hasKeyword(filter.getKeyword()))
                .and(ProductVariantSpecification.hasAttributes(filter.getSpecFilters()));

        Page<ProductVariant> pageResult = productVariantRepository.findAll(spec, pageable);
        List<ProductVariant> productVariants = pageResult.getContent();
        List<ProductVariantCardResponse> responseList = productVariants.stream()
                .map(productVariantMapper::toProductVariantResponse)
                .collect(Collectors.toList());

        return PageableResponse.<ProductVariantCardResponse>builder()
                .content(responseList)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .numberOfElements(pageResult.getNumberOfElements())
                .build();
    }

    private String mapSortFieldToColumn(String frontendField) {
        if (frontendField == null || frontendField.isBlank()) {
            return "variantName"; // mặc định sort theo tên variant
        }
        switch (frontendField.toLowerCase()) {
            case "price":
                return "price";
            case "name":
            case "variantName":
                return "variantName";
            default:
                log.warn("Unknown sort field: {}, using default variantName", frontendField);
                return "variantName";
        }
    }

    public ProductVariantDetailResponse getVariantDetail(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        ProductVariantDetailResponse result = new ProductVariantDetailResponse();
        result.setId(variantId);
        result.setVariantName(variant.getVariantName());
        result.setPrice(variant.getPrice());
        result.setStockQuantity(variant.getStockQuantity());

        Long finalPrice = variant.getPrice();
        Discount discount = variant.getDiscount();
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

        result.setFinalPrice(finalPrice);
        result.setDiscountText(discountText.toString());

        List<String> images = variantImageRepository.findByVariantIdOrderMainFirst(variantId).stream()
                .map(VariantImage::getImageUrl)
                .toList();
        result.setImages(images);

        List<VariantSpecResponse> specs = variantSpecificationRepository.findByVariantId(variantId).stream()
                .map(s -> VariantSpecResponse.builder()
                        .attributeId(s.getAttribute().getId())
                        .name(s.getAttribute().getName())
                        .value(s.getSpecValue())
                        .build())
                .toList();
        result.setVariantSpecs(specs);

        return result;
    }


    public ProductVariantResponse createVariant(ProductVariantRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED, Map.of("id", req.getProductId())));

        ProductVariant v = ProductVariant.builder()
                .variantName(req.getVariantName())
                .price(req.getPrice())
                .stockQuantity(req.getStockQuantity())
                .product(product)
                .build();

        return productVariantMapper.toResponse(productVariantRepository.save(v));
    }

    public ProductVariantResponse updateVariant(Long id, ProductVariantRequest req) {
        ProductVariant v = productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

        v.setVariantName(req.getVariantName());
        v.setPrice(req.getPrice());
        v.setStockQuantity(req.getStockQuantity());

        return productVariantMapper.toResponse(productVariantRepository.save(v));
    }

    public void deleteVariant(Long id) {
        if (!productVariantRepository.existsById(id)) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED);
        }
        productVariantRepository.deleteById(id);
    }

    public void updateStock(Long id, Integer stock) {
        ProductVariant v = productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        v.setStockQuantity(stock);
        productVariantRepository.save(v);
    }

}
