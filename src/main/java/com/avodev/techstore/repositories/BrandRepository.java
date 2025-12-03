package com.avodev.techstore.repositories;

import com.avodev.techstore.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    @Query("SELECT DISTINCT b FROM Brand b " +
            "JOIN Product p ON p.brand.id = b.id " +
            "WHERE p.category.id = :categoryId")
    List<Brand> findBrandsByCategory(@Param("categoryId") Long categoryId);
}
