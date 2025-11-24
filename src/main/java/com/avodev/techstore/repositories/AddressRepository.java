package com.avodev.techstore.repositories;

import com.avodev.techstore.entities.Address;
import com.avodev.techstore.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUser(User user);

    Optional<Address> findByUserAndIsDefaultTrue(User user);
}
