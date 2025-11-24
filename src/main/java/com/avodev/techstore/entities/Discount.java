package com.avodev.techstore.entities;


import com.avodev.techstore.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    DiscountType type;

    @Column(name = "value", nullable = false)
    BigDecimal value;

    @Column(name = "start_date", nullable = false)
    LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @OneToOne
    @JoinColumn(name = "product_variant_id", nullable = false)
    ProductVariant productVariant;
}
