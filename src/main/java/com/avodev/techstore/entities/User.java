package com.avodev.techstore.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;


@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "fullname", length = 250)
    String fullName;

    @Column(name = "phone_number", length = 10, nullable = false, unique = true)
    String phoneNumber;

    @Column(name = "address", length = 250)
    String address;

    @Column(name = "password", length = 100, nullable = false)
    String password;

    @Column(name = "is_active")
    boolean active;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    @Column(name = "facebook_account_id")
    int facebookAccountId;

    @Column(name = "google_account_id")
    int googleAccountId;

    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;

}
