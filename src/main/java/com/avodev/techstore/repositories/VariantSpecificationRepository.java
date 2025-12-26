package com.avodev.techstore.repositories;

import com.avodev.techstore.entities.VariantSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantSpecificationRepository extends JpaRepository<VariantSpecification, Long> {
    @Query("""
                SELECT s FROM VariantSpecification s
                JOIN FETCH s.attribute a
                WHERE s.variant.id = :variantId
            """)
    List<VariantSpecification> findByVariantId(@Param("variantId") Long variantId);
}
