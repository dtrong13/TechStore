package com.avodev.techstore.enums;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@Getter
public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác");


    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public static Map<String, String> getGender() {
        Map<String, String> map = new TreeMap<>();
        for (Gender gender : values()) {
            map.put(gender.name(), gender.getLabel());
        }
        return map;
    }

    public static Gender fromLabel(String label) {
        for (Gender type : values()) {
            if (type.getLabel().equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid gender: " + label);
    }
}
