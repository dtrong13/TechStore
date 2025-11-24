package com.avodev.techstore.responses;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    Long id;
    String recipientName;
    String phoneNumber;
    String province;
    String commune;
    String addressType;
    String addressDetail;
    Boolean isDefault;

}
