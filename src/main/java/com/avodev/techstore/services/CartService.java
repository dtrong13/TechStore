package com.avodev.techstore.services;


import com.avodev.techstore.entities.Cart;
import com.avodev.techstore.entities.CartItem;
import com.avodev.techstore.entities.ProductVariant;
import com.avodev.techstore.entities.User;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.mappers.ProductVariantMapper;
import com.avodev.techstore.repositories.CartItemRepository;
import com.avodev.techstore.repositories.CartRepository;
import com.avodev.techstore.repositories.ProductVariantRepository;
import com.avodev.techstore.repositories.UserRepository;
import com.avodev.techstore.responses.CartItemResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {
    CartRepository cartRepository;
    UserRepository userRepository;
    CartItemRepository cartItemRepository;
    ProductVariantRepository productVariantRepository;
    ProductVariantMapper productVariantMapper;


    private User getCurrentUser() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private Cart getCurrentCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    return cartRepository.save(c);
                });
    }

    public List<CartItemResponse> getCartItems() {
        User currentUser = getCurrentUser();
        Cart currentCart = getCurrentCart(currentUser);
        List<CartItem> cartItems = cartItemRepository.findByCart(currentCart);
        List<CartItemResponse> resultList = new ArrayList<>();
        for (CartItem item : cartItems) {
            CartItemResponse response = new CartItemResponse();
            response.setProductVariant(productVariantMapper.toProductVariantResponse(item.getVariant()));
            response.setQuantity(item.getQuantity());
            resultList.add(response);
        }
        return resultList;
    }

    @Transactional
    public void addItem(Long variantId, Integer quantity) {
        User user = getCurrentUser();
        Cart cart = getCurrentCart(user);
        if (quantity == null || quantity < 1) {
            quantity = 1;
        }
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        CartItem item = cartItemRepository.findByCartAndVariantId(cart, variantId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setVariant(variant);
                    newItem.setQuantity(0);
                    return newItem;
                });
        int newQuantity = item.getQuantity() + quantity;
        if (newQuantity > variant.getStockQuantity()) {
            throw new AppException(
                    ErrorCode.NOT_ENOUGH_STOCK,
                    Map.of("availableStock", variant.getStockQuantity())
            );
        }
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateItemQuantity(Long variantId, Integer quantity) {
        User user = getCurrentUser();
        Cart cart = getCurrentCart(user);

        if (quantity == null || quantity <= 0) {
            removeItems(List.of(variantId));
            return;
        }
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

        if (quantity > variant.getStockQuantity()) {
            throw new AppException(
                    ErrorCode.NOT_ENOUGH_STOCK,
                    Map.of("availableStock", variant.getStockQuantity())
            );
        }
        CartItem item = cartItemRepository.findByCartAndVariantId(cart, variantId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setVariant(variant);
                    return newItem;
                });

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }


    @Transactional
    public void removeItems(List<Long> variantIds) {
        User currentUser = getCurrentUser();
        Cart currentCart = getCurrentCart(currentUser);
        List<CartItem> itemsToDelete = cartItemRepository.findByCartAndVariantIdIn(currentCart, variantIds);
        cartItemRepository.deleteAll(itemsToDelete);
    }

    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();
        Cart currentCart = getCurrentCart(currentUser);
        cartItemRepository.deleteAll(currentCart.getItems());
    }

    public int getItemCount() {
        User currentUser = getCurrentUser();
        Cart currentCart = getCurrentCart(currentUser);
        List<CartItem> cartItems = cartItemRepository.findByCart(currentCart);
        return cartItems.size();
    }

}
