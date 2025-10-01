package com.avodev.techstore.services;


import com.avodev.techstore.entities.Permission;
import com.avodev.techstore.entities.Role;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.mappers.RoleMapper;
import com.avodev.techstore.repositories.PermissionRepository;
import com.avodev.techstore.repositories.RoleRepository;
import com.avodev.techstore.requests.RoleRequest;
import com.avodev.techstore.responses.RoleResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse createRole(RoleRequest roleRequest) {
        if (roleRepository.existsByName(roleRequest.getName())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        Role role = roleMapper.toRole(roleRequest);
        List<Permission> permissions = permissionRepository.findAllByNameIn(roleRequest.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    public void deleteRole(String roleName) {
        if (!permissionRepository.existsByName(roleName)) {
            throw new AppException(ErrorCode.ROLE_NOT_EXISTED);
        }
        permissionRepository.deleteByName(roleName);
    }

}
