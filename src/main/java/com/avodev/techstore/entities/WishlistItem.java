package com.avodev.techstore.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"wishlist_id", "variant_id"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WishlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "wishlist_id", nullable = false)
    Wishlist wishlist;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    ProductVariant variant;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
