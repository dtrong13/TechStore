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
import com.avodev.techstore.responses.ProductVariantResponse;
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
import java.util.Random;

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
        long millis = System.currentTimeMillis();
        int random = new Random().nextInt(100);
        return "TS" + String.format("%06d%02d", millis % 1000000, random);
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        OrderResponse preview = previewOrder(orderRequest);
        Order order = new Order();
        User currentUser = getCurrentUser();
        order.setUser(currentUser);
        Address address = addressRepository.findByUserAndId(currentUser, orderRequest.getShippingAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));
        order.setAddress(address);
        order.setCustomerNote(orderRequest.getCustomerNote());
        LocalDateTime now = LocalDateTime.now();
        order.setOrderDate(now);
        order.setTotalMoney(preview.getTotalMoney());
        order.setDeliveryMethod(DeliveryMethod.fromLabel(orderRequest.getDeliveryMethod()));
        String trackingNumber = generateTrackingNumber();
        order.setTrackingNumber(trackingNumber);
        PaymentMethod paymentMethod = PaymentMethod.fromLabel(orderRequest.getPaymentMethod());
        order.setPaymentMethod(paymentMethod);
        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            order.setStatus(OrderStatus.PENDING);  // chờ xác nhận
        } else {
            order.setStatus(OrderStatus.WAITING);  // tạm thời coi như thanh toán đã thành công
        }

        Order orderSaved = orderRepository.save(order);

        List<OrderItemRequest> orderItemRequests = orderRequest.getItems();
        for (OrderItemRequest item : orderItemRequests) {
            ProductVariant variant = productVariantRepository.findByIdForUpdate(item.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new AppException(ErrorCode.NOT_ENOUGH_STOCK);
            }

            variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
            productVariantRepository.save(variant);
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(orderSaved);
            orderDetail.setProductVariant(variant);
            orderDetail.setQuantity(item.getQuantity());
            ProductVariantResponse productVariantResponse = productVariantMapper.toProductVariantResponse(variant);
            orderDetail.setPrice(productVariantResponse.getFinalPrice());
            orderDetailRepository.save(orderDetail);
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
        preview.setOrderDate(now);
        preview.setStatus(order.getStatus().getLabel());
        preview.setTrackingNumber(trackingNumber);
        return preview;
    }

    public OrderResponse previewOrder(OrderRequest orderRequest) {
        User currentUser = getCurrentUser();
        List<OrderItemRequest> requestItems = orderRequest.getItems();
        List<OrderItemResponse> responseItems = new ArrayList<>();
        for (OrderItemRequest item : requestItems) {
            OrderItemResponse responseItem = new OrderItemResponse();
            ProductVariant productVariant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
            ProductVariantResponse productVariantResponse = productVariantMapper.toProductVariantResponse(productVariant);
            if (productVariant.getStockQuantity() < item.getQuantity()) {
                throw new AppException(ErrorCode.NOT_ENOUGH_STOCK);
            }
            productVariantResponse.setStockQuantity(productVariant.getStockQuantity() - item.getQuantity());
            Long finalPrice = productVariantResponse.getFinalPrice();
            Integer quantity = item.getQuantity();
            responseItem.setQuantity(quantity);
            responseItem.setProductVariant(productVariantResponse);
            responseItem.setTotalItemPrice(finalPrice * quantity);
            responseItems.add(responseItem);
        }

        Long subtotal = responseItems.stream()
                .mapToLong(OrderItemResponse::getTotalItemPrice)
                .sum();

        Address address = addressRepository.findByUserAndId(currentUser, orderRequest.getShippingAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        DeliveryMethod deliveryMethod = DeliveryMethod.fromLabel(orderRequest.getDeliveryMethod());

        Long shippingFee = calculateShippingFee(subtotal, address, deliveryMethod);

        Long totalMoney = subtotal + shippingFee;

        OrderResponse response = new OrderResponse();
        response.setDeliveryAddress(addressMapper.toAddressResponse(address));
        response.setCustomerNote(orderRequest.getCustomerNote());
        response.setDeliveryMethod(orderRequest.getDeliveryMethod());
        response.setPaymentMethod(orderRequest.getPaymentMethod());
        response.setShippingFee(shippingFee);
        response.setSubtotal(subtotal);
        response.setTotalMoney(totalMoney);
        response.setItems(responseItems);

        return response;

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



