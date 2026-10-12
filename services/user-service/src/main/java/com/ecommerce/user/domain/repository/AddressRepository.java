package com.ecommerce.user.domain.repository;

import com.ecommerce.user.domain.model.aggregate.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserIdOrderByDefaultAddressDescCreatedAtAsc(UUID userId);
    long countByUserId(UUID userId);
}
