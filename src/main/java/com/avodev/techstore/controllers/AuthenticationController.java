package com.avodev.techstore.controllers;

import com.avodev.techstore.requests.ChangePasswordRequest;
import com.avodev.techstore.requests.LoginRequest;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.LoginResponse;
import com.avodev.techstore.services.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {

    AuthenticationService authenticationService;

    // ------------------- LOGIN -------------------
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        var loginResult = authenticationService.login(loginRequest);

        // Lưu refresh token vào HttpOnly cookie
        String refreshToken = loginResult.getRefreshToken();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(loginRequest.isRememberMe() ? 7 * 24 * 60 * 60 : -1) // 7 ngày nếu rememberMe
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // Chỉ trả accessToken + authenticated
        LoginResponse loginResponse = LoginResponse.builder()
                .authenticated(true)
                .accessToken(loginResult.getAccessToken())
                .build();

        return ApiResponse.<LoginResponse>builder()
                .message("Đăng nhập thành công")
                .data(loginResponse)
                .build();
    }

    // ------------------- REFRESH TOKEN -------------------
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse response) {

        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            return ApiResponse.<LoginResponse>builder()
                    .message("Refresh token không tồn tại")
                    .build();
        }

        var loginResult = authenticationService.refreshToken(refreshTokenCookie);

        // Lưu refresh token mới vào cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResult.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // Chỉ trả accessToken + authenticated
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(loginResult.getAccessToken())
                .authenticated(true)
                .build();

        return ApiResponse.<LoginResponse>builder()
                .data(loginResponse)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse response) {

        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            authenticationService.logout(refreshTokenCookie);
        }

        // Xóa cookie trên client
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ApiResponse.<Void>builder()
                .message("Đăng xuất thành công")
                .build();
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse response) {

        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            authenticationService.logoutAll(refreshTokenCookie);
        }

        // Xóa cookie hiện tại trên client
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ApiResponse.<Void>builder()
                .message("Đăng xuất tất cả thiết bị thành công")
                .build();
    }


    // ------------------- CHANGE PASSWORD -------------------
    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ApiResponse.<Void>builder()
                .message("Đổi mật khẩu thành công")
                .build();
    }
}
