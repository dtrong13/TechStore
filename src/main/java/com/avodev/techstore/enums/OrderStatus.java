package com.avodev.techstore.enums;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@Getter
public enum OrderStatus {
    PENDING("Chờ xác nhận"),
    PROCESSING("Đang xử lý"),
    SHIPPED("Đã giao cho đơn vị vận chuyển"),
    DELIVERED("Đã giao thành công"),
    CANCELLED("Đã hủy");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public static Map<String, String> getStatus() {
        Map<String, String> map = new TreeMap<>();
        for (OrderStatus status : values()) {
            map.put(status.name(), status.getLabel());
        }
        return map;
    }
}
