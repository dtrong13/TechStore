package com.avodev.techstore.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    private Map<String, Object> meta = new HashMap<>();

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, Map<String, Object> meta) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.meta = meta;
    }
}
