package com.avodev.techstore.entities;

import com.avodev.techstore.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "fullname", length = 250)
    String fullName;

    @Column(name = "phone_number", length = 10, nullable = false, unique = true)
    String phoneNumber;

    @Column(name = "password", length = 100, nullable = false)
    String password;

    @Column(name = "is_active")
    boolean active;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    Gender gender = Gender.MALE;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Address> addresses;

    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;


}
