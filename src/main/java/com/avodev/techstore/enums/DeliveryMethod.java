package com.avodev.techstore.enums;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@Getter
public enum DeliveryMethod {
    STANDARD("Giao hàng tiêu chuẩn"),
    EXPRESS("Giao hàng nhanh"),
    LOWCOST("Giao hàng tiết kiệm");

    private final String label;

    DeliveryMethod(String label) {
        this.label = label;
    }

    public static Map<String, String> getDeliveryMethod() {
        Map<String, String> map = new TreeMap<>();
        for (DeliveryMethod deliveryMethod : values()) {
            map.put(deliveryMethod.name(), deliveryMethod.getLabel());
        }
        return map;
    }
}
