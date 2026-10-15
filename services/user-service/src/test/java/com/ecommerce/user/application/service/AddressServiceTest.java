package com.ecommerce.user.application.service;

import com.ecommerce.user.application.dto.UpdateAddressRequest;
import com.ecommerce.user.domain.model.aggregate.Address;
import com.ecommerce.user.domain.model.valueobjects.GeographicAddress;
import com.ecommerce.user.domain.repository.AddressRepository;
import com.ecommerce.user.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {
    @Mock AddressRepository addressRepository;
    @Mock CustomerRepository customerRepository;
    @InjectMocks AddressService addressService;

    @Test
    void promotesAnotherAddressWhenDefaultIsUnset() {
        UUID userId = UUID.randomUUID();
        Address first = address(userId, 1L, true);
        Address second = address(userId, 2L, false);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(first));
        when(addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId))
                .thenReturn(List.of(first, second));
        when(addressRepository.saveAndFlush(first)).thenReturn(first);

        addressService.update(1L, request(false));

        assertThat(first.isDefaultAddress()).isFalse();
        assertThat(second.isDefaultAddress()).isTrue();
    }

    @Test
    void keepsTheOnlyAddressAsDefault() {
        UUID userId = UUID.randomUUID();
        Address only = address(userId, 1L, true);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(only));
        when(addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId))
                .thenReturn(List.of(only));
        when(addressRepository.saveAndFlush(only)).thenReturn(only);

        addressService.update(1L, request(false));

        assertThat(only.isDefaultAddress()).isTrue();
    }

    private Address address(UUID userId, Long id, boolean isDefault) {
        Address address = new Address(userId, "Test User", "0901234567",
                new GeographicAddress("123 Test St", null, null, "Hanoi", null), isDefault);
        ReflectionTestUtils.setField(address, "id", id);
        return address;
    }

    private UpdateAddressRequest request(boolean isDefault) {
        return new UpdateAddressRequest("Test User", "0901234567", "456 Test St",
                null, null, "Hanoi", null, isDefault);
    }
}
