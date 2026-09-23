package com.ecommerce.user.application.service;

import com.ecommerce.user.application.dto.AddressResponse;
import com.ecommerce.user.application.dto.CreateAddressRequest;
import com.ecommerce.user.application.dto.UpdateAddressRequest;
import com.ecommerce.user.domain.model.aggregate.Address;
import com.ecommerce.user.domain.model.valueobjects.GeographicAddress;
import com.ecommerce.user.domain.repository.AddressRepository;
import com.ecommerce.user.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public AddressService(AddressRepository addressRepository, CustomerRepository customerRepository) {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
    }

    public AddressResponse create(CreateAddressRequest request) {
        requireUser(request.userId());
        boolean makeDefault = request.defaultAddress() || addressRepository.countByUserId(request.userId()) == 0;
        if (makeDefault) clearDefault(request.userId(), null);

        Address address = new Address(request.userId(), request.receiverName().trim(), request.receiverPhone().trim(),
                geography(request.addressLine(), request.ward(), request.district(), request.city(), request.postalCode()),
                makeDefault);
        return toResponse(addressRepository.saveAndFlush(address));
    }

    @Transactional(readOnly = true)
    public AddressResponse get(long id) {
        return toResponse(load(id));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listByUser(UUID userId) {
        requireUser(userId);
        return addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId)
                .stream().map(this::toResponse).toList();
    }

    public AddressResponse update(long id, UpdateAddressRequest request) {
        Address address = load(id);
        if (request.defaultAddress()) clearDefault(address.getUserId(), id);

        address.setReceiverName(request.receiverName().trim());
        address.setReceiverPhone(request.receiverPhone().trim());
        address.setGeographicAddress(geography(request.addressLine(), request.ward(), request.district(), request.city(), request.postalCode()));
        address.setDefaultAddress(request.defaultAddress());

        if (!request.defaultAddress() && addressRepository.countByUserId(address.getUserId()) == 1) {
            address.setDefaultAddress(true);
        }
        return toResponse(addressRepository.saveAndFlush(address));
    }

    public void delete(long id) {
        Address address = load(id);
        UUID userId = address.getUserId();
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);
        addressRepository.flush();
        if (wasDefault) {
            addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId).stream().findFirst()
                    .ifPresent(next -> next.setDefaultAddress(true));
        }
    }

    private void clearDefault(UUID userId, Long exceptId) {
        addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId).stream()
                .filter(Address::isDefaultAddress)
                .filter(address -> exceptId == null || !address.getId().equals(exceptId))
                .forEach(address -> address.setDefaultAddress(false));
    }

    private void requireUser(UUID userId) {
        if (!customerRepository.existsById(userId)) {
            throw new UserNotFoundException("User %s was not found".formatted(userId));
        }
    }

    private Address load(long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Address %d was not found".formatted(id)));
    }

    private GeographicAddress geography(String line, String ward, String district, String city, String postalCode) {
        return new GeographicAddress(line.trim(), trimToNull(ward), trimToNull(district), city.trim(), trimToNull(postalCode));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AddressResponse toResponse(Address address) {
        GeographicAddress geo = address.getGeographicAddress();
        return new AddressResponse(address.getId(), address.getUserId(), address.getReceiverName(), address.getReceiverPhone(),
                geo.getAddressLine(), geo.getWard(), geo.getDistrict(), geo.getCity(), geo.getPostalCode(),
                address.isDefaultAddress(), address.getCreatedAt(), address.getUpdatedAt());
    }
}
