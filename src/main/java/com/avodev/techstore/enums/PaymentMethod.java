package com.avodev.techstore.enums;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@Getter
public enum PaymentMethod {
    CASH_ON_DELIVERY("Thanh toán khi nhận hàng"),
    E_WALLET("Thanh toán bằng ví điện tử"),
    BANK_TRANSFER("Thanh toán bằng chuyển khoản ngân hàng"),
    CREDIT_CARD("Thanh toán bằng thẻ tín dụng");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public static Map<String, String> getPaymentMethod() {
        Map<String, String> map = new TreeMap<>();
        for (PaymentMethod paymentMethod : values()) {
            map.put(paymentMethod.name(), paymentMethod.getLabel());
        }
        return map;
    }

    public static PaymentMethod fromLabel(String label) {
        for (PaymentMethod method : values()) {
            if (method.getLabel().equalsIgnoreCase(label)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Invalid payment method: " + label);
    }
}
