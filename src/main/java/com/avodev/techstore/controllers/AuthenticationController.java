package com.avodev.techstore.controllers;


import com.avodev.techstore.entities.User;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.repositories.UserRepository;
import com.avodev.techstore.requests.AuthenticationRequest;

import com.avodev.techstore.requests.LogoutRequest;
import com.avodev.techstore.requests.RefreshRequest;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.AuthenticationResponse;

import com.avodev.techstore.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    UserRepository userRepository;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) {
        var result = authenticationService.login(authenticationRequest);
        User user = userRepository.findByPhoneNumber(authenticationRequest.getPhoneNumber())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String refreshToken = authenticationService.generateRefreshToken(user);
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();
    }


    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(
            @CookieValue(name = "refreshToken") String refreshTokenCookie)
            throws ParseException, JOSEException {
        RefreshRequest request = RefreshRequest.builder()
                .token(refreshTokenCookie)
                .build();
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
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

        return ApiResponse.<Void>builder().build();
    }


}
