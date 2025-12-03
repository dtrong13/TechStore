package com.avodev.techstore.repositories;


import com.avodev.techstore.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    String findTrackingNumberById(Long orderId);


}
