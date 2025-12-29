package com.avodev.techstore.services;


import com.avodev.techstore.entities.*;
import com.avodev.techstore.enums.DeliveryMethod;
import com.avodev.techstore.enums.OrderStatus;
import com.avodev.techstore.enums.PaymentMethod;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.mappers.AddressMapper;
import com.avodev.techstore.mappers.ProductVariantMapper;
import com.avodev.techstore.repositories.*;
import com.avodev.techstore.requests.OrderItemRequest;
import com.avodev.techstore.requests.OrderRequest;
import com.avodev.techstore.responses.OrderItemResponse;
import com.avodev.techstore.responses.OrderResponse;
import com.avodev.techstore.responses.ProductVariantCardResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    CartRepository cartRepository;
    OrderDetailRepository orderDetailRepository;
    CartItemRepository cartItemRepository;
    ProductVariantRepository productVariantRepository;
    UserRepository userRepository;
    AddressRepository addressRepository;
    ProductVariantMapper productVariantMapper;
    AddressMapper addressMapper;

    private User getCurrentUser() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private String generateTrackingNumber() {
        return "TS-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    @Transactional(readOnly = true)
    public OrderResponse previewOrder(OrderRequest orderRequest) {
        User currentUser = getCurrentUser();

        List<OrderItemResponse> responseItems =
                buildOrderItems(orderRequest.getItems(), false);

        Long subtotal = responseItems.stream()
                .mapToLong(OrderItemResponse::getTotalItemPrice)
                .sum();

        Address address = addressRepository
                .findByUserAndId(currentUser, orderRequest.getShippingAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        DeliveryMethod deliveryMethod =
                DeliveryMethod.fromLabel(orderRequest.getDeliveryMethod());

        Long shippingFee = calculateShippingFee(subtotal, address, deliveryMethod);

        OrderResponse response = new OrderResponse();
        response.setDeliveryAddress(addressMapper.toAddressResponse(address));
        response.setCustomerNote(orderRequest.getCustomerNote());
        response.setDeliveryMethod(orderRequest.getDeliveryMethod());
        response.setPaymentMethod(orderRequest.getPaymentMethod());
        response.setShippingFee(shippingFee);
        response.setSubtotal(subtotal);
        response.setTotalMoney(subtotal + shippingFee);
        response.setItems(responseItems);

        return response;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {

        // build + lock + validate + tính tiền
        List<OrderItemResponse> responseItems =
                buildOrderItems(orderRequest.getItems(), true);

        Long subtotal = responseItems.stream()
                .mapToLong(OrderItemResponse::getTotalItemPrice)
                .sum();

        User currentUser = getCurrentUser();
        Address address = addressRepository.findByUserAndId(
                        currentUser, orderRequest.getShippingAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        DeliveryMethod deliveryMethod =
                DeliveryMethod.fromLabel(orderRequest.getDeliveryMethod());

        Long shippingFee = calculateShippingFee(subtotal, address, deliveryMethod);
        Long totalMoney = subtotal + shippingFee;

        Order order = new Order();
        order.setUser(currentUser);
        order.setAddress(address);
        order.setCustomerNote(orderRequest.getCustomerNote());
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryMethod(deliveryMethod);
        order.setPaymentMethod(PaymentMethod.fromLabel(orderRequest.getPaymentMethod()));
        order.setTotalMoney(totalMoney);
        order.setTrackingNumber(generateTrackingNumber());
        order.setStatus(OrderStatus.PENDING);

        Order orderSaved = orderRepository.save(order);

        // tạo order details + trừ kho
        for (int i = 0; i < orderRequest.getItems().size(); i++) {
            OrderItemRequest reqItem = orderRequest.getItems().get(i);
            OrderItemResponse respItem = responseItems.get(i);

            ProductVariant variant = productVariantRepository
                    .findByIdForUpdate(reqItem.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

            variant.setStockQuantity(
                    variant.getStockQuantity() - reqItem.getQuantity()
            );

            OrderDetail detail = new OrderDetail();
            detail.setOrder(orderSaved);
            detail.setProductVariant(variant);
            detail.setQuantity(reqItem.getQuantity());
            detail.setPrice(respItem.getProductVariant().getFinalPrice());

            orderDetailRepository.save(detail);
        }

        if (Boolean.TRUE.equals(orderRequest.getFromCart())) {
            List<Long> variantIds = orderRequest.getItems().stream()
                    .map(OrderItemRequest::getVariantId)
                    .toList();
            Cart cart = cartRepository.findByUser(currentUser)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

            List<CartItem> cartItems = cartItemRepository.findByCartAndVariantIn(cart, variantIds);
            cartItemRepository.deleteAll(cartItems);
        }

        OrderResponse response = new OrderResponse();
        response.setItems(responseItems);
        response.setSubtotal(subtotal);
        response.setShippingFee(shippingFee);
        response.setTotalMoney(totalMoney);
        response.setOrderDate(orderSaved.getOrderDate());
        response.setStatus(orderSaved.getStatus().getLabel());
        response.setTrackingNumber(orderSaved.getTrackingNumber());

        return response;
    }


    private List<OrderItemResponse> buildOrderItems(List<OrderItemRequest> items, boolean forUpdate
    ) {
        List<OrderItemResponse> responseItems = new ArrayList<>();

        for (OrderItemRequest item : items) {
            ProductVariant variant = forUpdate
                    ? productVariantRepository.findByIdForUpdate(item.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED))
                    : productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new AppException(ErrorCode.NOT_ENOUGH_STOCK);
            }

            ProductVariantCardResponse variantResp =
                    productVariantMapper.toProductVariantResponse(variant);

            if (forUpdate) {
                variantResp.setStockQuantity(
                        variant.getStockQuantity() - item.getQuantity()
                );
            }


            Long finalPrice = variantResp.getFinalPrice();
            Integer quantity = item.getQuantity();

            OrderItemResponse responseItem = new OrderItemResponse();
            responseItem.setQuantity(quantity);
            responseItem.setProductVariant(variantResp);
            responseItem.setTotalItemPrice(finalPrice * quantity);

            responseItems.add(responseItem);
        }

        return responseItems;
    }


    private Long calculateShippingFee(long subtotal, Address address, DeliveryMethod deliveryMethod) {
        // Miễn phí nếu subtotal >= 1 triệu
        if (subtotal >= 1_000_000L) return 0L;

        // Xác định khu vực của người dùng
        String region = getRegion(address);

        // Tính phí theo phương thức vận chuyển
        return switch (deliveryMethod) {
            case DeliveryMethod.EXPRESS -> getExpressFee(region);
            case DeliveryMethod.LOWCOST -> getLowCostFee(region);
            default -> getStandardFee(region);
        };
    }

    private String getRegion(Address address) {
        String province = address.getProvince().toLowerCase();
        String commune = address.getCommune().toLowerCase();

        // Hà Nội
        if (province.contains("hà nội")) {
            // Danh sách ngoại thành Hà Nội
            List<String> outerDistricts = Arrays.asList(
                    "thạch thất", "đông anh", "gia lâm", "hoài đức",
                    "quốc oai", "ba vì", "đan phượng", "mê linh",
                    "sơn tây", "thường tín"
            );

            if (outerDistricts.contains(commune)) {
                return "HN_OUT";
            }
            return "HN_IN"; // còn lại nội thành
        }

        // Miền Bắc
        List<String> northProvinces = Arrays.asList(
                "tuyên quang", "lào cai", "thái nguyên", "phú thọ",
                "bắc ninh", "hưng yên", "ninh bình", "hải phòng",
                "lai châu", "điện biên", "sơn la", "lạng sơn",
                "quảng ninh", "cao bằng"
        );

        if (northProvinces.contains(province)) {
            return "NORTH";
        }

        // Mặc định OTHER
        return "OTHER";
    }

    // Phí Standard theo khu vực
    private Long getStandardFee(String region) {
        return switch (region) {
            case "HN_IN" -> 15000L;
            case "HN_OUT" -> 20000L;
            case "NORTH" -> 25000L;
            default -> 30000L;
        };
    }

    // Phí Express theo khu vực
    private Long getExpressFee(String region) {
        return switch (region) {
            case "HN_IN" -> 30000L;
            case "HN_OUT" -> 40000L;
            case "NORTH" -> 50000L;
            default -> 60000L;
        };
    }

    // Phí LowCost theo khu vực
    private Long getLowCostFee(String region) {
        return switch (region) {
            case "HN_IN" -> 10000L;
            case "HN_OUT" -> 15000L;
            case "NORTH" -> 20000L;
            default -> 25000L;
        };
    }


}



