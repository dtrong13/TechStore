package com.avodev.techstore.enums;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@Getter
public enum AddressType {
    HOME("Nhà riêng"),
    OFFICE("Văn phòng"),
    OTHER("Khác");

    private final String label;

    AddressType(String label) {
        this.label = label;
    }

    public static Map<String, String> getAddressType() {
        Map<String, String> map = new TreeMap<>();
        for (AddressType addressType : values()) {
            map.put(addressType.name(), addressType.getLabel());
        }
        return map;
    }

    public static AddressType fromLabel(String label) {
        for (AddressType type : values()) {
            if (type.getLabel().equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid address type: " + label);
    }
}
