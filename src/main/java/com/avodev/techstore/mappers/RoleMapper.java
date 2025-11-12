package com.avodev.techstore.mappers;

import com.avodev.techstore.entities.Role;
import com.avodev.techstore.requests.RoleRequest;
import com.avodev.techstore.responses.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);

}
