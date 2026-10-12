package com.ecommerce.order.api;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.exception.OrderIntegrationException;
import com.ecommerce.order.exception.OrderNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ecommerce.order")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OrderExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiResponse<Void> notFound(OrderNotFoundException exception) { return ApiResponse.error(exception.getMessage()); }

    @ExceptionHandler(OrderIntegrationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiResponse<Void> integration(OrderIntegrationException exception) { return ApiResponse.error(exception.getMessage()); }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiResponse<Void> illegalState(IllegalStateException exception) { return ApiResponse.error(exception.getMessage()); }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiResponse<Void> denied(AccessDeniedException exception) { return ApiResponse.error(exception.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiResponse<Void> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed");
        return ApiResponse.error(message);
    }
}
