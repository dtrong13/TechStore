package com.avodev.techstore.services;


import com.avodev.techstore.entities.ProductVariant;
import com.avodev.techstore.entities.User;
import com.avodev.techstore.entities.Wishlist;
import com.avodev.techstore.entities.WishlistItem;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.mappers.ProductVariantMapper;
import com.avodev.techstore.repositories.ProductVariantRepository;
import com.avodev.techstore.repositories.UserRepository;
import com.avodev.techstore.repositories.WishlistItemRepository;
import com.avodev.techstore.repositories.WishlistRepository;
import com.avodev.techstore.responses.ProductVariantCardResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WishlistService {
    WishlistRepository wishlistRepo;
    WishlistItemRepository wishlistItemRepo;
    ProductVariantRepository productVariantRepo;
    UserRepository userRepository;
    ProductVariantMapper productVariantMapper;


    private User getCurrentUser() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private Wishlist getCurrentWishlist(User user) {
        return wishlistRepo.findByUser(user)
                .orElseGet(() -> {
                    Wishlist w = new Wishlist();
                    w.setUser(user);
                    return wishlistRepo.save(w);
                });
    }

    public List<ProductVariantCardResponse> getWishlistItems() {
        User currentUser = getCurrentUser();
        Wishlist currentWishlist = getCurrentWishlist(currentUser);
        List<WishlistItem> wishlistItems = wishlistItemRepo.findByWishlist(currentWishlist);
        List<ProductVariantCardResponse> resultList = new ArrayList<>();
        for (WishlistItem item : wishlistItems) {
            ProductVariantCardResponse response = productVariantMapper.toProductVariantResponse(item.getVariant());
            resultList.add(response);
        }
        return resultList;
    }

    @Transactional
    public void addItem(Long variantId) {
        User currentUser = getCurrentUser();
        Wishlist currentWishlist = getCurrentWishlist(currentUser);
        wishlistItemRepo.findByWishlistAndVariantId(currentWishlist, variantId)
                .orElseGet(() -> {
                    WishlistItem item = new WishlistItem();
                    item.setWishlist(currentWishlist);
                    ProductVariant variant = productVariantRepo.findById(variantId)
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
                    item.setVariant(variant);
                    return wishlistItemRepo.save(item);
                });
    }

    @Transactional
    public void removeItems(List<Long> variantIds) {
        User currentUser = getCurrentUser();
        Wishlist currentWishlist = getCurrentWishlist(currentUser);
        List<WishlistItem> itemsToDelete = wishlistItemRepo.findByWishlistAndVariantIdIn(currentWishlist, variantIds);
        wishlistItemRepo.deleteAll(itemsToDelete);
    }

    @Transactional
    public void clearWishlist() {
        User currentUser = getCurrentUser();
        Wishlist currentWishlist = getCurrentWishlist(currentUser);
        wishlistItemRepo.deleteAll(currentWishlist.getItems());
    }


    public int getItemCount() {
        User currentUser = getCurrentUser();
        Wishlist currentWishlist = getCurrentWishlist(currentUser);
        List<WishlistItem> wishlistItems = wishlistItemRepo.findByWishlist(currentWishlist);
        return wishlistItems.size();
    }


}
