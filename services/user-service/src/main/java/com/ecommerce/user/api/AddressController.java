package com.ecommerce.user.api;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.user.application.dto.AddressResponse;
import com.ecommerce.user.application.dto.CreateAddressRequest;
import com.ecommerce.user.application.dto.UpdateAddressRequest;
import com.ecommerce.user.application.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> create(@Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(addressService.create(request), "Address created"));
    }

    @GetMapping
    public ApiResponse<List<AddressResponse>> listByUser(@RequestParam UUID userId) {
        return ApiResponse.ok(addressService.listByUser(userId));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<AddressResponse>> listByUserPath(@PathVariable UUID userId) {
        return ApiResponse.ok(addressService.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<AddressResponse> get(@PathVariable long id) {
        return ApiResponse.ok(addressService.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> update(@PathVariable long id,
                                               @Valid @RequestBody UpdateAddressRequest request) {
        return ApiResponse.ok(addressService.update(id, request), "Address updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        addressService.delete(id);
        return ApiResponse.ok(null, "Address deleted");
    }
}
