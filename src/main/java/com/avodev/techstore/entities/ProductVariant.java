package com.avodev.techstore.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariant extends BaseEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;

    @Column(name = "variant_name",  nullable = false, length = 250)
    String variantName;

    @Column(name = "storage", nullable = false, length = 50)
    String storage;

    @Column(name = "color", nullable = false, length = 50)
    String color;

    @Column(name = "price", nullable = false)
    BigDecimal price;

    @Column(name = "stock_quantity")
    Integer stockQuantity;

}

