package com.avodev.techstore.services;

import com.avodev.techstore.entities.InvalidatedToken;
import com.avodev.techstore.entities.Role;
import com.avodev.techstore.entities.User;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.repositories.InvalidatedTokenRepository;
import com.avodev.techstore.repositories.UserRepository;
import com.avodev.techstore.requests.*;
import com.avodev.techstore.responses.IntrospectResponse;
import com.avodev.techstore.responses.LoginResponse;
import com.avodev.techstore.responses.LoginResult;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;


    public LoginResult login(LoginRequest loginRequest) {
        User user = userRepository.findByPhoneNumber(loginRequest.getPhoneNumber())
                .orElse(null);
        if (user == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_REGISTERED); // tài khoản chưa đăng ký
        }

        if (!user.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE); // tài khoản đã bị khóa/chưa kích hoạt
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        String accessToken = generateToken(user, VALID_DURATION, "access");
        String refreshToken = generateToken(user, REFRESHABLE_DURATION, "refresh");
        return LoginResult.builder()
                .authenticated(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        String token = request.getToken();
        if (token == null || token.isBlank()) {
            log.info("No refresh token to invalidate");
            return;
        }

        try {
            var signToken = verifyToken(request.getToken(), "refresh");

            String jit = signToken.getJWTClaimsSet().getJWTID();
            LocalDate expiryTime = signToken.getJWTClaimsSet()
                    .getExpirationTime()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, "access");
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public LoginResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        String token = request.getToken();
        if (token == null || token.isBlank()) {
            User guestUser = User.builder()
                    .phoneNumber("anonymous")
                    .fullName("Guest")
                    .role(Role.builder().name("GUEST").build()) // role GUEST
                    .build();

            String dummyToken = generateToken(guestUser, VALID_DURATION, "access");

            return LoginResponse.builder()
                    .accessToken(dummyToken)
                    .authenticated(false)
                    .build();
        }


        var signedJWT = verifyToken(request.getToken(), "refresh");

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByPhoneNumber(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String accesToken = generateToken(user, VALID_DURATION, "access");

        return LoginResponse.builder()
                .accessToken(accesToken)
                .authenticated(true).build();
    }

    public void changePassword(ChangePasswordRequest request) {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (currentUser == null || !passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }

    private String generateToken(User user, long expirySeconds, String type) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getPhoneNumber())
                .issuer("avodev.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(expirySeconds)))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", type)
                .claim("scope", "access".equals(type) ? buildScope(user) : null)
                .claim("fullName", user.getFullName())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot sign JWS object", e);
            throw new RuntimeException(e);
        }
    }


    private String buildScope(User user) {
        return "ROLE_" + user.getRole().getName();
    }

    private SignedJWT verifyToken(String token, String tokenType) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        String type = signedJWT.getJWTClaimsSet().getStringClaim("type");
        if (!tokenType.equals(type)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return signedJWT;
    }


}
