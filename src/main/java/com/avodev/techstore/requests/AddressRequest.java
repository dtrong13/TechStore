package com.avodev.techstore.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {
    String recipientName;
    String phoneNumber;
    String province;
    String commune;
    String addressType;
    String addressDetail;
    Boolean isDefault;
}
