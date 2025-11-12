package com.avodev.techstore.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;



@Entity
@Table(name = "invalidated_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidatedToken {
    @Id
    String id;

    @Column(name = "expiry_time")
    LocalDate expiryTime;
}
