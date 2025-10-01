package com.avodev.techstore.requests;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 8, message = "INVALID_FULLNAME")
    String fullName;

    @Pattern(regexp = "\\d{10}", message = "INVALID_PHONENUMBER")
    String phoneNumber;

    String address;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;

    LocalDate dateOfBirth;
}
