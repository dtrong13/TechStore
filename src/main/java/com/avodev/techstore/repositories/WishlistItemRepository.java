package com.avodev.techstore.repositories;

import com.avodev.techstore.entities.Wishlist;
import com.avodev.techstore.entities.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    Optional<WishlistItem> findByWishlistAndVariantId(Wishlist wishlist, Long variantId);

    List<WishlistItem> findByWishlistAndVariantIdIn(Wishlist wishlist, List<Long> variantIds);

    List<WishlistItem> findByWishlist(Wishlist wishlist);
}
