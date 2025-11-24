package com.avodev.techstore.controllers;


import com.avodev.techstore.requests.ChangePasswordRequest;
import com.avodev.techstore.requests.LoginRequest;
import com.avodev.techstore.requests.LogoutRequest;
import com.avodev.techstore.requests.RefreshRequest;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.LoginResponse;
import com.avodev.techstore.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        var loginResult = authenticationService.login(loginRequest);

        String refreshToken = loginResult.getRefreshToken();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("auth/refresh")
                .maxAge(loginRequest.isRememberMe() ? 7 * 24 * 60 * 60 : -1)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        log.info(String.valueOf(refreshCookie));
        LoginResponse loginResponse = LoginResponse.builder()
                .authenticated(true)
                .accessToken(loginResult.getAccessToken())
                .build();

        return ApiResponse.<LoginResponse>builder()
                .message("Đăng nhập thành công")
                .data(loginResponse)
                .build();
    }


    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @CookieValue(name = "refreshToken") String refreshTokenCookie)
            throws ParseException, JOSEException {
        RefreshRequest request = RefreshRequest.builder()
                .token(refreshTokenCookie)
                .build();
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<LoginResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = "refreshToken") String refreshTokenCookie,
            HttpServletResponse response) throws ParseException, JOSEException {

        // Invalidate
        LogoutRequest request = LogoutRequest.builder()
                .token(refreshTokenCookie)
                .build();
        authenticationService.logout(request);
        // Xóa cookie
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ApiResponse.<Void>builder()
                .message("Đăng xuất thành công")
                .build();
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ApiResponse.<Void>builder()
                .message("Đổi mật khẩu thành công")
                .build();
    }


}
