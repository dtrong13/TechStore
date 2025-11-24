package com.avodev.techstore.mappers;


import com.avodev.techstore.entities.User;
import com.avodev.techstore.requests.RegisterRequest;
import com.avodev.techstore.responses.RegisterResponse;
import com.avodev.techstore.responses.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User registerToUser(RegisterRequest registerRequest);

    RegisterResponse toRegisterResponse(User user);

    @Mapping(target = "gender", expression = "java(user.getGender().getLabel())")
    @Mapping(source = "phoneNumber", target = "hiddenPhone", qualifiedByName = "maskPhone")
    UserResponse toUserResponse(User user);

    @Named("maskPhone")
    default String maskPhone(String phone) {
        return phone.replaceAll("(?<=\\d{2})\\d", "*");
    }


}
