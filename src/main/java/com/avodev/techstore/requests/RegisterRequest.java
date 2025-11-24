package com.avodev.techstore.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank
    String fullName;

    @Pattern(regexp = "\\d{10}", message = "INVALID_PHONENUMBER")
    String phoneNumber;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;


}
