package com.avodev.techstore.controllers;


import com.avodev.techstore.requests.UserCreationRequest;
import com.avodev.techstore.requests.UserUpdateRequest;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.UserResponse;
import com.avodev.techstore.services.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest userCreationRequest) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(userCreationRequest))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .data(userService.getUsers())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") Long userId) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getUser(userId))
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getMyInfo())
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().data("User has been deleted").build();
    }
    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.updateUser(userId, request))
                .build();
    }







}
