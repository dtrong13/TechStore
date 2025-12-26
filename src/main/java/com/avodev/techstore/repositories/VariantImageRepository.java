package com.avodev.techstore.repositories;


import com.avodev.techstore.entities.VariantImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantImageRepository extends JpaRepository<VariantImage, Long> {
    @Query("SELECT v FROM VariantImage v WHERE v.variant.id = :variantId AND v.isPrimary = true")
    VariantImage findPrimaryByVariantId(@Param("variantId") Long variantId);

    @Query("""
                SELECT i FROM VariantImage i
                WHERE i.variant.id = :variantId
                ORDER BY i.isPrimary DESC, i.id ASC
            """)
    List<VariantImage> findByVariantIdOrderMainFirst(@Param("variantId") Long variantId);
}
