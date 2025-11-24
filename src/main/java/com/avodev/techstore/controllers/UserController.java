package com.avodev.techstore.controllers;


import com.avodev.techstore.requests.RegisterRequest;
import com.avodev.techstore.requests.UserDeleteRequest;
import com.avodev.techstore.requests.UserUpdateRequest;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.RegisterResponse;
import com.avodev.techstore.responses.UserResponse;
import com.avodev.techstore.services.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/register")
    ApiResponse<RegisterResponse> register(@RequestBody @Valid RegisterRequest registerRequest) {
        return ApiResponse.<RegisterResponse>builder()
                .data(userService.register(registerRequest))
                .message("Đăng ký thành công")
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getMyInfo())
                .build();
    }

    @PutMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(@RequestBody @Valid UserUpdateRequest request) {
        UserResponse response = userService.updateProfile(request);
        return ApiResponse.<UserResponse>builder()
                .data(response)
                .message("Cập nhật hồ sơ cá nhân thành công")
                .build();
    }

    @DeleteMapping("/account")
    public ApiResponse<Void> deleteAccount(@RequestBody UserDeleteRequest request) {
        userService.deleteAccount(request);
        return ApiResponse.<Void>builder()
                .message("Xóa tài khoản thành công")
                .build();
    }

//    @GetMapping
//    ApiResponse<List<RegisterResponse>> getUsers() {
//        return ApiResponse.<List<RegisterResponse>>builder()
//                .data(userService.getUsers())
//                .build();
//    }
//
//    @GetMapping("/{userId}")
//    ApiResponse<RegisterResponse> getUser(@PathVariable("userId") Long userId) {
//        return ApiResponse.<RegisterResponse>builder()
//                .data(userService.getUser(userId))
//                .build();
//    }

//
//    @DeleteMapping("/{userId}")
//    ApiResponse<String> deleteUser(@PathVariable Long userId) {
//        userService.deleteUser(userId);
//        return ApiResponse.<String>builder().data("User has been deleted").build();
//    }
//
//    @PutMapping("/{userId}")
//    ApiResponse<RegisterResponse> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
//        return ApiResponse.<RegisterResponse>builder()
//                .data(userService.updateUser(userId, request))
//                .build();
//    }


}
