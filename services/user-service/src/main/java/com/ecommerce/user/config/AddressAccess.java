package com.ecommerce.user.config;

import com.ecommerce.user.domain.repository.AddressRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("addressAccess")
public class AddressAccess {
    private final AddressRepository addressRepository;

    public AddressAccess(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public boolean isOwner(Long addressId, String subject) {
        try {
            return addressRepository.existsByIdAndUserId(addressId, UUID.fromString(subject));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
