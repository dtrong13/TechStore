package com.avodev.techstore.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // ================= Validation Errors =================
    INVALID_KEY(1001, "Invalid input key.", HttpStatus.BAD_REQUEST),
    INVALID_PHONENUMBER(1002, "Phone number must contain exactly 10 digits.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1003, "Password must be at least {min} characters long.", HttpStatus.BAD_REQUEST),
    INVALID_DOB(1004, "User must be at least {min} years old.", HttpStatus.BAD_REQUEST),
    INVALID_FULLNAME(1005, "Full name must be at least {min} characters long.", HttpStatus.BAD_REQUEST),

    // ================= User Errors =================
    USER_EXISTED(2001, "User already exists.", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(2002, "User not found.", HttpStatus.NOT_FOUND),

    PERMISSION_EXISTED(2003, "Permission already exists.", HttpStatus.NOT_FOUND),
    PERMISSION_NOT_EXISTED(2004, "Permission not found.", HttpStatus.BAD_REQUEST),

    ROLE_EXISTED(2003, "Role already exists.", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED(2004, "Role not found.", HttpStatus.BAD_REQUEST),


    // ================= Authentication / Authorization Errors =================
    UNAUTHENTICATED(3001, "Authentication required. Please log in.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(3002, "Access denied. Insufficient permissions.", HttpStatus.FORBIDDEN),

    // ================= System / Uncategorized Errors =================
    UNCATEGORIZED_EXCEPTION(9000, "Unexpected system error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);






    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message , HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
