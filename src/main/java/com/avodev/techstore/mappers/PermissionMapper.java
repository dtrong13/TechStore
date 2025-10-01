package com.avodev.techstore.mappers;


import com.avodev.techstore.entities.Permission;
import com.avodev.techstore.requests.PermissionRequest;
import com.avodev.techstore.responses.PermissionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission (PermissionRequest permissionRequest);
    PermissionResponse toPermissionResponse (Permission permission);
}
