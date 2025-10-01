package com.avodev.techstore.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "social_accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "provider", nullable = false, length = 20)
    String provider;

    @Column(name = "provider_id", nullable = false, length = 50)
    String providerId;

    @Column(name = "name",length = 150)
    String name;

    @Column(name = "email", length = 150)
    String email;

}
