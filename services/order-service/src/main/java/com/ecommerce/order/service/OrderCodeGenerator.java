package com.ecommerce.order.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class OrderCodeGenerator {

    private final AtomicLong lastValue = new AtomicLong(System.currentTimeMillis());

    public long next() {
        return lastValue.updateAndGet(previous ->
                Math.max(System.currentTimeMillis(), previous + 1)
        );
    }
}
