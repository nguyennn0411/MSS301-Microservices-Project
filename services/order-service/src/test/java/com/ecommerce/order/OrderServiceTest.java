package com.ecommerce.order;

import com.ecommerce.order.client.*;
import com.ecommerce.order.client.dto.*;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.*;
import com.ecommerce.order.shop.ShopSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orders;
    @Mock ProductCatalogClient catalog;
    @Mock PaymentClient payments;
    @Mock OrderCodeGenerator codes;
    @Mock ShopSettings settings;
    OrderService service;
    UUID userId=UUID.randomUUID(),productId=UUID.randomUUID(),variantId=UUID.randomUUID();
    @BeforeEach void init(){service=new OrderService(orders,catalog,payments,codes,settings);}
    CreateOrderRequest request(String method){return new CreateOrderRequest(userId,"Customer","buyer@example.com",List.of(new CreateOrderItemRequest(productId,2,variantId)),"0900000000","123 Street","Deliver at door",method,UUID.randomUUID());}
    void catalogPrice(){when(catalog.validate(any())).thenReturn(new ProductValidationResponse(true,List.of(new ProductValidationResponse.Item(productId,"Cotton tee",new BigDecimal("120000"),true,null,variantId,"M","Black")),"OK"));when(settings.shippingFee()).thenReturn(new BigDecimal("30000"));when(codes.next()).thenReturn(12345L);}
    @Test void codTotalsComeFromCatalogAndPersistVariantAndShipping(){
        catalogPrice();var request=request("COD");
        when(payments.createPayment(eq("COD"),any())).thenAnswer(invocation->{CreatePaymentRequest p=invocation.getArgument(1);assertThat(p.amount()).isEqualByComparingTo("270000");return new PaymentResponse(UUID.randomUUID(),p.orderId(),p.orderCode(),p.userId(),p.amount(),"VND","COD","PENDING",null,null,null,null,null,null,null);});
        var response=service.create(request);
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);assertThat(response.totalAmount()).isEqualByComparingTo("270000");
        assertThat(response.items()).hasSize(1);assertThat(response.items().getFirst().size()).isEqualTo("M");assertThat(response.shippingAddress()).isEqualTo("123 Street");
        verify(orders).saveAndFlush(any());verify(orders).save(any());
    }
    @Test void failedPaymentKeepsOrderAndRecordsFailure(){
        catalogPrice();when(payments.createPayment(eq("COD"),any())).thenThrow(new IllegalStateException("unavailable"));
        var response=service.create(request("COD"));
        assertThat(response.status()).isEqualTo(OrderStatus.PAYMENT_FAILED);verify(orders).save(any());
    }
    @Test void duplicateRequestKeyReturnsSavedOrderWithoutNewPayment(){
        var request=request("COD");var saved=com.ecommerce.order.domain.OrderEntity.create(123L,userId,"Customer","buyer@example.com","VND");
        when(orders.findByUserIdAndRequestKey(userId,request.requestKey())).thenReturn(Optional.of(saved));
        assertThat(service.create(request).id()).isEqualTo(saved.getId());verifyNoInteractions(catalog,payments);
    }
}
