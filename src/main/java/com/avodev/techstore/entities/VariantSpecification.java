package com.avodev.techstore.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "variant_specifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantSpecification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    ProductVariant variant;

    @ManyToOne
    @JoinColumn(name = "spec_attribute_id")
    VariantAttribute attribute;

    @Column(name = "spec_value")
    String specValue;
}
