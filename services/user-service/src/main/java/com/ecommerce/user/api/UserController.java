package com.ecommerce.user.api;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.user.application.dto.CreateUserRequest;
import com.ecommerce.user.application.dto.UpdateUserRequest;
import com.ecommerce.user.application.dto.UserResponse;
import com.ecommerce.user.application.dto.UserValidationResponse;
import com.ecommerce.user.application.service.CustomerService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CustomerService customerService;

    public UserController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(customerService.create(request), "User created"));
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> list() {
        return ApiResponse.ok(customerService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(customerService.get(id));
    }

    @GetMapping("/{id}/validation")
    public UserValidationResponse validate(@PathVariable UUID id) {
        try {
            UserResponse user = customerService.get(id);
            boolean active = "ACTIVE".equals(user.status());
            return new UserValidationResponse(id, active, active ? "User is active" : "User is inactive");
        } catch (com.ecommerce.user.application.service.UserNotFoundException exception) {
            return new UserValidationResponse(id, false, "User was not found");
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(customerService.update(id, request), "User updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<UserResponse> deactivate(@PathVariable UUID id) {
        return ApiResponse.ok(customerService.deactivate(id), "User deactivated");
    }
}
