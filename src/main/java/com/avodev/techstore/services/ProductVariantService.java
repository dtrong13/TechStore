package com.avodev.techstore.services;


import com.avodev.techstore.dtos.Pagination;
import com.avodev.techstore.dtos.SortField;
import com.avodev.techstore.entities.ProductVariant;
import com.avodev.techstore.enums.SortDirection;
import com.avodev.techstore.mappers.ProductVariantMapper;
import com.avodev.techstore.repositories.ProductVariantRepository;
import com.avodev.techstore.requests.ProductVariantRequest;
import com.avodev.techstore.requests.SearchRequest;
import com.avodev.techstore.responses.PageableResponse;
import com.avodev.techstore.responses.ProductVariantResponse;
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

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductVariantService {

    ProductVariantRepository productVariantRepository;
    VariantImageService variantImageService;
    ProductVariantMapper productVariantMapper;

    public PageableResponse<ProductVariantResponse> searchProductVariants(SearchRequest<ProductVariantRequest> request) {
        ProductVariantRequest filter = request.getFilter();
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
        DecimalFormat moneyFormat = new DecimalFormat("#,###");
        List<ProductVariant> productVariants = pageResult.getContent();
        List<ProductVariantResponse> responseList = productVariants.stream()
                .map(productVariantMapper::toProductVariantResponse)
                .collect(Collectors.toList());

        return PageableResponse.<ProductVariantResponse>builder()
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


}
