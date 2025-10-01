package com.avodev.techstore.mappers;


import com.avodev.techstore.entities.User;
import com.avodev.techstore.requests.UserCreationRequest;
import com.avodev.techstore.requests.UserUpdateRequest;
import com.avodev.techstore.responses.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest userCreationRequest);
    User toUser(UserUpdateRequest userUpdateRequest);
    UserResponse toUserResponse(User user);
}
