package com.avodev.techstore.mappers;


import com.avodev.techstore.entities.Address;
import com.avodev.techstore.enums.AddressType;
import com.avodev.techstore.requests.AddressRequest;
import com.avodev.techstore.responses.AddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "addressType", expression = "java(address.getAddressType().getLabel())")
    AddressResponse toAddressResponse(Address address);

    @Mapping(target = "addressType", source = "addressType", qualifiedByName = "stringToAddressType")
    Address toAddress(AddressRequest addressRequest);

    @Named("stringToAddressType")
    default AddressType stringToAddressType(String type) {
        if (type == null) return AddressType.HOME;
        return switch (type.toUpperCase()) {
            case "OFFICE" -> AddressType.OFFICE;
            default -> AddressType.HOME;
        };
    }
}
