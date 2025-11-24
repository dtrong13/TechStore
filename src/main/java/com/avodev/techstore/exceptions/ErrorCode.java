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
    INVALID_FULLNAME(1005, "Full name must be at least {min} characters long.", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS_TYPE(1005, "Invalid address type, please try again", HttpStatus.BAD_REQUEST),
    INVALID_GENDER_TYPE(1005, "Invalid gender type, please try again", HttpStatus.BAD_REQUEST),

    CANNOT_DELETE_DEFAULT_ADDRESS(1006, "Can not delete default address, please try again", HttpStatus.BAD_REQUEST),


    // ================= User Errors =================
    USER_EXISTED(2001, "User already exists.", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(2002, "User not found.", HttpStatus.NOT_FOUND),

    ROLE_EXISTED(2003, "Role already exists.", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED(2004, "Role not found.", HttpStatus.BAD_REQUEST),


    // ================= Authentication / Authorization Errors =================
    UNAUTHENTICATED(3001, "Authentication required. Please log in.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(3002, "Access denied. Insufficient permissions.", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(3003, "Incorrect phone number or password. Please try logging in again.", HttpStatus.UNAUTHORIZED),
    ACCOUNT_INACTIVE(3004, "Account has been deleted or is no longer active.", HttpStatus.FORBIDDEN),


    // ================= User Errors =================
    ADDRESS_NOT_EXISTED(4001, "Address not found.", HttpStatus.NOT_FOUND),

    // ================= Password Errors =================
    PASSWORD_INCORRECT(5001, "The current password is incorrect.", HttpStatus.BAD_REQUEST),
    PASSWORD_DUPLICATED(5002, "The new password cannot be the same as the current password..", HttpStatus.BAD_REQUEST),


    // ================= System / Uncategorized Errors =================
    UNCATEGORIZED_EXCEPTION(9000, "Unexpected system error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
