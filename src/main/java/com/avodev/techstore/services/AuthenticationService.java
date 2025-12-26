package com.avodev.techstore.services;

import com.avodev.techstore.entities.RefreshToken;
import com.avodev.techstore.entities.User;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.repositories.RefreshTokenRepository;
import com.avodev.techstore.repositories.UserRepository;
import com.avodev.techstore.requests.ChangePasswordRequest;
import com.avodev.techstore.requests.LoginRequest;
import com.avodev.techstore.responses.LoginResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;
    PasswordEncoder passwordEncoder;
    JwtEncoder jwtEncoder;
    JwtDecoder jwtDecoder;

    private final long VALID_DURATION = 3600; // 1h
    private final long REFRESHABLE_DURATION = 604800; // 7 days

    // ------------------- LOGIN -------------------
    @Transactional
    public LoginResult login(LoginRequest loginRequest) {
        User user = userRepository.findByPhoneNumber(loginRequest.getPhoneNumber())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_REGISTERED));

        if (!user.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = generateToken(user, VALID_DURATION, TokenType.ACCESS);
        String refreshToken = generateToken(user, REFRESHABLE_DURATION, TokenType.REFRESH);

        // Lưu refresh token vào DB
        Jwt refreshJwt = decodeToken(refreshToken);
        RefreshToken rt = RefreshToken.builder()
                .id(refreshJwt.getClaim("jti"))
                .user(user)
                .expiryTime(refreshJwt.getExpiresAt())
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);

        return LoginResult.builder()
                .authenticated(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.info("No refresh token to invalidate");
            return;
        }

        try {
            Jwt jwt = verifyToken(refreshToken, TokenType.REFRESH); // verify token hợp lệ
            String jti = jwt.getClaim("jti");

            RefreshToken rt = refreshTokenRepository.findById(jti)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

            rt.setRevoked(true); // revoke token hiện tại
            refreshTokenRepository.save(rt);

        } catch (AppException e) {
            log.info("Refresh token invalid or already revoked");
        }
    }

    @Transactional
    public void logoutAll(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.info("No refresh token to logout all");
            return;
        }

        try {
            Jwt jwt = verifyToken(refreshToken, TokenType.REFRESH); // verify token hợp lệ
            String phoneNumber = jwt.getSubject();

            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            refreshTokenRepository.revokeAllByUser(user); // revoke tất cả refresh token của user

        } catch (AppException e) {
            log.info("Refresh token invalid, cannot logout all");
        }
    }


    // ------------------- REFRESH TOKEN -------------------
    @Transactional
    public LoginResult refreshToken(String oldRefreshToken) {
        if (oldRefreshToken == null || oldRefreshToken.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Verify token cũ
        Jwt oldJwt = verifyToken(oldRefreshToken, TokenType.REFRESH);
        String phoneNumber = oldJwt.getSubject();

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Revoke token cũ
        RefreshToken oldRt = refreshTokenRepository.findById(oldJwt.getClaim("jti"))
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        oldRt.setRevoked(true);
        refreshTokenRepository.save(oldRt);

        // Tạo token mới
        String newAccessToken = generateToken(user, VALID_DURATION, TokenType.ACCESS);
        String newRefreshToken = generateToken(user, REFRESHABLE_DURATION, TokenType.REFRESH);

        Jwt newJwt = decodeToken(newRefreshToken);
        RefreshToken newRt = RefreshToken.builder()
                .id(newJwt.getClaim("jti"))
                .user(user)
                .expiryTime(newJwt.getExpiresAt())
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRt);

        return LoginResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .build();
    }


    private User getCurrentUser() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    // ------------------- CHANGE PASSWORD -------------------
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUser(user);
    }

    // ------------------- TOKEN UTIL -------------------
    private String generateToken(User user, long expirySeconds, TokenType type) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("avodev.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirySeconds))
                .subject(user.getPhoneNumber())
                .claim("type", type.name())
                .claim("scope", user.getRole().getName())
                .claim("jti", UUID.randomUUID().toString())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS512).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims))
                .getTokenValue();
    }

    private Jwt decodeToken(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private Jwt verifyToken(String token, TokenType expectedType) {
        Jwt jwt = decodeToken(token);
        String typeClaim = jwt.getClaim("type");
        if (!expectedType.name().equals(typeClaim)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (expectedType == TokenType.REFRESH) {
            String jti = jwt.getClaim("jti");
            RefreshToken rt = refreshTokenRepository.findById(jti)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
            if (rt.isRevoked() || rt.getExpiryTime().isBefore(Instant.now())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }

        return jwt;
    }

    public enum TokenType {ACCESS, REFRESH}
}
