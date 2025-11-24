package com.avodev.techstore.configurations;

import com.avodev.techstore.constant.PredefineRole;
import com.avodev.techstore.entities.Role;
import com.avodev.techstore.entities.User;
import com.avodev.techstore.repositories.RoleRepository;
import com.avodev.techstore.repositories.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    static final String ADMIN_PHONE_NUMBER = "0000000000";
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            log.info("Init application ...");

            // Tạo role ADMIN nếu chưa tồn tại
            Role adminRole = roleRepository.findByName(PredefineRole.ADMIN_ROLE)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(PredefineRole.ADMIN_ROLE)
                            .build()));

            // Tạo admin user nếu chưa tồn tại
            if (userRepository.findByPhoneNumber(ADMIN_PHONE_NUMBER).isEmpty()) {
                User user = User.builder()
                        .phoneNumber(ADMIN_PHONE_NUMBER)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .role(adminRole)
                        .active(true)
                        .build();
                try {
                    userRepository.save(user);
                    log.warn("Admin user created with phoneNumber {} and a default password. Please change it immediately.",
                            ADMIN_PHONE_NUMBER);
                } catch (DataIntegrityViolationException e) {
                    log.info("Admin user creation skipped due to concurrent creation.");
                }
            }

            log.info("Application initialization completed.");
        };
    }

}
