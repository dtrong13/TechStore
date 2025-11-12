package com.avodev.techstore.controllers;


import com.avodev.techstore.requests.RoleRequest;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.RoleResponse;
import com.avodev.techstore.services.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> createRole(@RequestBody RoleRequest roleRequest) {
        return ApiResponse.<RoleResponse>builder()
                .data(roleService.createRole(roleRequest))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAllRoles() {
        return ApiResponse.<List<RoleResponse>>builder()
                .data(roleService.getAllRoles())
                .build();
    }

    @DeleteMapping("/{role}")
    ApiResponse<Void> deleteRole(@PathVariable String role) {
        roleService.deleteRole(role);
        return ApiResponse.<Void>builder()
                .message("Role deleted successfully.")
                .build();
    }

}
