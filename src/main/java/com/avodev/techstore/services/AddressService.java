package com.avodev.techstore.services;


import com.avodev.techstore.entities.Address;
import com.avodev.techstore.entities.User;
import com.avodev.techstore.enums.AddressType;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.mappers.AddressMapper;
import com.avodev.techstore.repositories.AddressRepository;
import com.avodev.techstore.repositories.UserRepository;
import com.avodev.techstore.requests.AddressRequest;
import com.avodev.techstore.responses.AddressResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressService {
    AddressRepository addressRepository;
    UserRepository userRepository;
    AddressMapper addressMapper;

    private User getCurrentUser() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public List<AddressResponse> getAllAddresses() {
        User currentUser = getCurrentUser();
        List<Address> userAddresses = addressRepository.findAllByUser(currentUser);
        return userAddresses.stream()
                .map(addressMapper::toAddressResponse)
                .toList();

    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        User currentUser = getCurrentUser();
        Address newAddress = addressMapper.toAddress(request);
        newAddress.setUser(currentUser);
        Optional<Address> currentDefault = addressRepository.findByUserAndIsDefaultTrue(currentUser);
        if (Boolean.TRUE.equals(newAddress.getIsDefault())) {
            currentDefault.ifPresent(address -> {
                address.setIsDefault(false);
                addressRepository.save(address);
            });
        }
        Address saved = addressRepository.save(newAddress);
        return addressMapper.toAddressResponse(saved);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        address.setRecipientName(request.getRecipientName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setProvince(request.getProvince());
        address.setCommune(request.getCommune());
        address.setAddressDetail(request.getAddressDetail());


        try {
            address.setAddressType(AddressType.fromLabel(request.getAddressType()));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_ADDRESS_TYPE);
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            Optional<Address> currentDefault = addressRepository.findByUserAndIsDefaultTrue(currentUser);
            currentDefault.ifPresent(a -> {
                a.setIsDefault(false);
                addressRepository.save(a);
            });
        }

        address.setIsDefault(request.getIsDefault());
        Address saved = addressRepository.save(address);
        return addressMapper.toAddressResponse(saved);
    }

    @Transactional
    public void deleteAddress(Long id) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            throw new AppException(ErrorCode.CANNOT_DELETE_DEFAULT_ADDRESS);
        }

        addressRepository.delete(address);
    }

    @Transactional
    public List<AddressResponse> setDefaultAddress(Long addressId) {
        User currentUser = getCurrentUser();
        Optional<Address> currentDefault = addressRepository.findByUserAndIsDefaultTrue(currentUser);
        currentDefault.ifPresent(address -> {
            address.setIsDefault(false);
            addressRepository.save(address);
        });
        Address newDefault = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));
        newDefault.setIsDefault(true);
        addressRepository.save(newDefault);
        List<Address> allAddresses = addressRepository.findAllByUser(currentUser);
        return allAddresses.stream()
                .map(addressMapper::toAddressResponse)
                .toList();
    }
}
