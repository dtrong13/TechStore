package com.avodev.techstore.controllers;

import com.avodev.techstore.requests.AddressRequest;
import com.avodev.techstore.responses.AddressResponse;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.services.AddressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressController {
    AddressService addressService;


    @GetMapping
    public ApiResponse<List<AddressResponse>> getAllAddresses() {
        List<AddressResponse> result = addressService.getAllAddresses();
        return ApiResponse.<List<AddressResponse>>builder()
                .data(result)
                .build();
    }

    @PostMapping
    public ApiResponse<AddressResponse> createAddress(@RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(request);
        return ApiResponse.<AddressResponse>builder()
                .data(response)
                .message("Thêm địa chỉ mới thành công")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(@PathVariable Long id,
                                                      @RequestBody AddressRequest request) {
        AddressResponse response = addressService.updateAddress(id, request);
        return ApiResponse.<AddressResponse>builder()
                .data(response)
                .message("Cập nhật địa chỉ thành công")
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ApiResponse.<Void>builder()
                .message("Xóa địa chỉ thành công")
                .build();
    }

    @PatchMapping("/{id}/default")
    public ApiResponse<List<AddressResponse>> setDefaultAddress(@PathVariable("id") Long id) {
        List<AddressResponse> updatedAddresses = addressService.setDefaultAddress(id);
        return ApiResponse.<List<AddressResponse>>builder()
                .data(updatedAddresses)
                .build();
    }
}
