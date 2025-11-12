package com.avodev.techstore.services;

import com.avodev.techstore.entities.InvalidatedToken;
import com.avodev.techstore.entities.User;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.repositories.InvalidatedTokenRepository;
import com.avodev.techstore.repositories.UserRepository;
import com.avodev.techstore.requests.AuthenticationRequest;
import com.avodev.techstore.requests.IntrospectRequest;
import com.avodev.techstore.requests.LogoutRequest;
import com.avodev.techstore.requests.RefreshRequest;
import com.avodev.techstore.responses.AuthenticationResponse;
import com.avodev.techstore.responses.IntrospectResponse;
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

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
        User user = userRepository.findByPhoneNumber(authenticationRequest.getPhoneNumber())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String accessToken = generateToken(user, VALID_DURATION, "access");
        return AuthenticationResponse.builder()
                .authenticated(true)
                .accessToken(accessToken)
                .build();
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

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESHABLE_DURATION, "refresh");
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), "refresh");

            String jit = signToken.getJWTClaimsSet().getJWTID();
            LocalDate expiryTime = signToken.getJWTClaimsSet()
                    .getExpirationTime()                  // trả về java.util.Date
                    .toInstant()                          // chuyển sang Instant
                    .atZone(ZoneId.systemDefault())       // gắn múi giờ (ZoneId)
                    .toLocalDate();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), "refresh");

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByPhoneNumber(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String accesToken = generateToken(user, VALID_DURATION, "access");

        return AuthenticationResponse.builder()
                .accessToken(accesToken)
                .authenticated(true).build();
    }


    private String buildScope(User user) {
        return "ROLE_" + user.getRole().toString();
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
