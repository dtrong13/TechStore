package com.avodev.techstore.entities;

import com.avodev.techstore.enums.DeliveryMethod;
import com.avodev.techstore.enums.OrderStatus;
import com.avodev.techstore.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "address", nullable = false, length = 250)
    String address;

    @Column(name = "customer_note")
    String customerNote;

    @Column(name = "order_date")
    LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    OrderStatus status;

    @Column(name = "total_money")
    BigDecimal totalMoney;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method")
    DeliveryMethod deliveryMethod;

    @Column(name = "delivery_date")
    LocalDate deliveryDate;

    @Column(name = "tracking_number", nullable = false, length = 100, unique = true)
    String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    PaymentMethod paymentMethod;

    @Column(name = "is_active")
    Boolean isActive;
}
