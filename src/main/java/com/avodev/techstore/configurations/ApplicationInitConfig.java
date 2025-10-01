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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    static final String ADMIN_PHONE_NUMBER = "0000000000";
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(prefix = "spring", value = "datasource.driverClassName", havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            log.info("Init application ...");
            if (!roleRepository.existsByName(PredefineRole.USER_ROLE)) {
                roleRepository.save(Role.builder()
                        .name(PredefineRole.USER_ROLE)
                        .description("User role")
                        .build());
            }
            Role adminRole = roleRepository.findByName(PredefineRole.ADMIN_ROLE)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(PredefineRole.ADMIN_ROLE)
                            .description("Admin role")
                            .build()));
            if (userRepository.findByPhoneNumber(ADMIN_PHONE_NUMBER).isEmpty()) {
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                roleRepository.findByName(PredefineRole.USER_ROLE).ifPresent(roles::add);
                User user = User.builder()
                        .phoneNumber(ADMIN_PHONE_NUMBER)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
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
