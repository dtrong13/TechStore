package com.avodev.techstore.enums;

import java.util.Map;
import java.util.TreeMap;

public enum PaymentMethod {
    CASH_ON_DELIVERY("Thanh toán khi nhận hàng"),
    E_WALLET("Thanh toán bằng điện tử"),
    BANK_TRANSFER("Thanh toán bằng chuyển khoản ngân hàng"),
    CREDIT_CARD("Thanh toán bằng thẻ tín dụng");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Map<String, String> getPaymentMethod() {
        Map<String, String> map = new TreeMap<>();
        for (PaymentMethod paymentMethod : values()) {
            map.put(paymentMethod.name(), paymentMethod.getLabel());
        }
        return map;
    }
}
