package com.avodev.techstore.entities;


import com.avodev.techstore.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "recipient_name", nullable = false, length = 250)
    String recipientName;

    @Column(name = "phone_number", nullable = false, length = 10)
    String phoneNumber;

    @Column(nullable = false, length = 250)
    String province;

    @Column(length = 250)
    String commune;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", columnDefinition = "ENUM('office', 'home')")
    AddressType addressType = AddressType.HOME;

    @Column(length = 250)
    String addressDetail;

    @Builder.Default
    @Column(name = "is_default")
    Boolean isDefault = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

}
