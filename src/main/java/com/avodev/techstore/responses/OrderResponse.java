package com.avodev.techstore.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    AddressResponse deliveryAddress;
    String customerNote;
    String deliveryMethod;
    String paymentMethod;
    String status;
    LocalDateTime orderDate;
    BigDecimal totalMoney;
    String trackingNumber;
    List<OrderItemResponse> items;
}
