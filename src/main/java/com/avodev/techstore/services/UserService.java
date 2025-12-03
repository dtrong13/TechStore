package com.avodev.techstore.services;

import com.avodev.techstore.constant.PredefineRole;
import com.avodev.techstore.entities.Role;
import com.avodev.techstore.entities.User;
import com.avodev.techstore.enums.Gender;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.mappers.AddressMapper;
import com.avodev.techstore.mappers.UserMapper;
import com.avodev.techstore.repositories.RoleRepository;
import com.avodev.techstore.repositories.UserRepository;
import com.avodev.techstore.requests.RegisterRequest;
import com.avodev.techstore.requests.UserDeleteRequest;
import com.avodev.techstore.requests.UserUpdateRequest;
import com.avodev.techstore.responses.RegisterResponse;
import com.avodev.techstore.responses.UserResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    AddressMapper addressMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.registerToUser(registerRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        Role role = roleRepository.findByName(PredefineRole.USER_ROLE).orElseGet(() -> roleRepository.save(Role.builder()
                .name(PredefineRole.USER_ROLE)
                .build()));
        user.setRole(role);
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        return userMapper.toRegisterResponse(user);
    }

    public UserResponse getMyInfo() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String phoneNumber = authentication.getName();
        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }


    @Transactional
    public UserResponse updateProfile(UserUpdateRequest request) {
        // Lấy user hiện tại từ security context
        User currentUser = getCurrentUser();
        currentUser.setFullName(request.getFullName());
        currentUser.setDateOfBirth(request.getDateOfBirth());
        try {
            currentUser.setGender(Gender.fromLabel(request.getGender()));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_GENDER_TYPE);
        }
        User saved = userRepository.save(currentUser);
        return userMapper.toUserResponse(saved);

    }

    private User getCurrentUser() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional
    public void deleteAccount(UserDeleteRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !passwordEncoder.matches(request.getPassword(), currentUser.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }
        currentUser.setActive(false);
        userRepository.save(currentUser);
    }

//
//
//    @PreAuthorize("hasRole('ADMIN')")
//    public List<RegisterResponse> getUsers() {
//        log.info("In method get Users");
//        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    public RegisterResponse getUser(Long id) {
//        return userMapper.toUserResponse(
//                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
//    }


}
