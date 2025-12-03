package com.avodev.techstore.specifications;

import com.avodev.techstore.entities.ProductVariant;
import com.avodev.techstore.entities.VariantSpecification;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class ProductVariantSpecification {
    private ProductVariantSpecification() {
        throw new UnsupportedOperationException("This is a utility class");
    }

    public static Specification<ProductVariant> hasProductId(Long productId) {
        return (root, query, cb) -> {
            if (productId == null) return cb.conjunction();
            return cb.equal(root.get("product").get("id"), productId);
        };
    }

    public static Specification<ProductVariant> hasBrandId(Long brandId) {
        return (root, query, cb) -> {
            if (brandId == null) return cb.conjunction();
            return cb.equal(root.get("product").get("brand").get("id"), brandId);
        };
    }

    public static Specification<ProductVariant> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            return cb.equal(root.get("product").get("category").get("id"), categoryId);
        };
    }

    public static Specification<ProductVariant> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return cb.conjunction();

            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get("price"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            } else {
                return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            }
        };
    }

    public static Specification<ProductVariant> inStock(Boolean inStock) {
        return (root, query, cb) -> {
            if (inStock == null) return cb.conjunction();
            if (inStock) {
                return cb.greaterThan(root.get("stockQuantity"), 0);
            } else {
                return cb.conjunction();
            }
        };
    }

    public static Specification<ProductVariant> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) return cb.conjunction();
            String like = "%" + keyword.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("variantName")), like);
        };
    }

    public static Specification<ProductVariant> hasAttributes(Map<Long, List<String>> specFilters) {
        return (root, query, cb) -> {
            if (specFilters == null || specFilters.isEmpty()) {
                return cb.conjunction();
            }

            Predicate finalPredicate = cb.conjunction();

            for (Map.Entry<Long, List<String>> e : specFilters.entrySet()) {
                Long attributeId = e.getKey();
                List<String> values = e.getValue();
                if (attributeId == null || values == null || values.isEmpty()) {
                    finalPredicate = cb.and(finalPredicate, cb.disjunction());
                    continue;
                }

                Subquery<Long> sq = query.subquery(Long.class);
                Root<VariantSpecification> vs = sq.from(VariantSpecification.class);
                sq.select(vs.get("id"));

                Predicate p1 = cb.equal(vs.get("variant").get("id"), root.get("id"));
                Predicate p2 = cb.equal(vs.get("attribute").get("id"), attributeId);
                Predicate p3 = vs.get("specValue").in(values);

                sq.where(cb.and(p1, p2, p3));

                finalPredicate = cb.and(finalPredicate, cb.exists(sq));
            }

            return finalPredicate;
        };
    }
}
