package com.ecommerce.payment;

import com.ecommerce.payment.domain.*;
import com.ecommerce.payment.dto.*;
import com.ecommerce.payment.gateway.PayOSGateway;
import com.ecommerce.payment.messaging.PaymentEventPublisher;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock PaymentRepository payments;
    @Mock PayOSGateway gateway;
    @Mock PaymentEventPublisher publisher;
    PaymentService service;
    UUID orderId=UUID.randomUUID(), userId=UUID.randomUUID();
    long code=System.currentTimeMillis();
    @BeforeEach void init(){service=new PaymentService(payments,gateway,publisher);}
    CreatePayOSPaymentRequest request(){return new CreatePayOSPaymentRequest(orderId,code,userId,"Customer","buyer@example.com",new BigDecimal("10000"),"VND");}
    @Test void codIsPersistedAndRepeatedRequestReturnsSamePayment(){
        var stored=new java.util.concurrent.atomic.AtomicReference<PaymentEntity>();
        when(payments.findByOrderId(orderId)).thenAnswer(invocation->Optional.ofNullable(stored.get()));
        when(payments.saveAndFlush(any(PaymentEntity.class))).thenAnswer(invocation->{stored.set(invocation.getArgument(0));return stored.get();});
        var first=service.createCodPayment(request());var second=service.createCodPayment(request());
        assertThat(first.provider()).isEqualTo("COD");assertThat(first.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(second.id()).isEqualTo(first.id());verify(payments,times(1)).saveAndFlush(any(PaymentEntity.class));
    }
    @Test void idempotentRequestRejectsChangedUser(){
        var payment=PaymentEntity.create(orderId,code,UUID.randomUUID(),"Customer","buyer@example.com",new BigDecimal("10000"),"VND");payment.useCod();
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        assertThatThrownBy(()->service.createCodPayment(request())).isInstanceOf(IllegalArgumentException.class);
        verify(payments,never()).saveAndFlush(any());
    }
    @Test void codCancellationDoesNotCallPayosAndPublishesEvent(){
        var payment=PaymentEntity.create(orderId,code,userId,"Customer","buyer@example.com",new BigDecimal("10000"),"VND");payment.useCod();
        when(payments.findByOrderCode(code)).thenReturn(Optional.of(payment));
        var result=service.cancel(code,new CancelPayOSPaymentRequest("Customer request"));
        assertThat(result.status()).isEqualTo(PaymentStatus.CANCELLED);
        verifyNoInteractions(gateway);verify(publisher).publishCancelled(payment);
    }
    @Test void verifiedWebhookIsIdempotent(){
        var payment=PaymentEntity.create(orderId,code,userId,"Customer","buyer@example.com",new BigDecimal("10000"),"VND");
        Webhook webhook=mock(Webhook.class);WebhookData data=mock(WebhookData.class);
        when(gateway.verifyWebhook(webhook)).thenReturn(data);when(data.getOrderCode()).thenReturn(code);when(data.getCode()).thenReturn("00");when(data.getReference()).thenReturn("bank-ref");
        when(payments.findByOrderCode(code)).thenReturn(Optional.of(payment));
        assertThat(service.handleWebhook(webhook).status()).isEqualTo(PaymentStatus.PAID);
        assertThat(service.handleWebhook(webhook).status()).isEqualTo(PaymentStatus.PAID);
        verify(payments,times(1)).save(payment);verify(publisher,times(1)).publishSuccess(payment);
    }
    @Test void cancelledPaymentCannotBeReopenedByDelayedWebhook(){
        var payment=PaymentEntity.create(orderId,code,userId,"Customer","buyer@example.com",new BigDecimal("10000"),"VND");payment.markCancelled("Cancelled");
        Webhook webhook=mock(Webhook.class);WebhookData data=mock(WebhookData.class);
        when(gateway.verifyWebhook(webhook)).thenReturn(data);when(data.getOrderCode()).thenReturn(code);when(payments.findByOrderCode(code)).thenReturn(Optional.of(payment));
        assertThat(service.handleWebhook(webhook).status()).isEqualTo(PaymentStatus.CANCELLED);
        verify(payments,never()).save(any());verifyNoInteractions(publisher);
    }
    @Test void verifiesPaidLinkWithPayOSBeforeMarkingPaymentPaid(){
        var payment=PaymentEntity.create(orderId,code,userId,"Customer","buyer@example.com",new BigDecimal("10000"),"VND");
        payment.attachPaymentLink("link-123","https://pay.payos.vn/web/link-123","qr");
        PaymentLink link=mock(PaymentLink.class);
        when(payments.findByOrderCode(code)).thenReturn(Optional.of(payment));
        when(gateway.getPaymentLink(code)).thenReturn(link);
        when(link.getOrderCode()).thenReturn(code);
        when(link.getId()).thenReturn("link-123");
        when(link.getAmount()).thenReturn(10000L);
        when(link.getAmountPaid()).thenReturn(10000L);
        when(link.getStatus()).thenReturn(PaymentLinkStatus.PAID);

        assertThat(service.verifyPayOSPayment(code).status()).isEqualTo(PaymentStatus.PAID);
        assertThat(service.verifyPayOSPayment(code).status()).isEqualTo(PaymentStatus.PAID);
        verify(gateway,times(1)).getPaymentLink(code);
        verify(payments,times(1)).save(payment);
        verify(publisher,times(1)).publishSuccess(payment);
    }
    @Test void mismatchedPayOSLinkCannotMarkPaymentPaid(){
        var payment=PaymentEntity.create(orderId,code,userId,"Customer","buyer@example.com",new BigDecimal("10000"),"VND");
        payment.attachPaymentLink("expected-link","https://pay.payos.vn/web/expected-link","qr");
        PaymentLink link=mock(PaymentLink.class);
        when(payments.findByOrderCode(code)).thenReturn(Optional.of(payment));
        when(gateway.getPaymentLink(code)).thenReturn(link);
        when(link.getOrderCode()).thenReturn(code);
        when(link.getId()).thenReturn("other-link");

        assertThatThrownBy(()->service.verifyPayOSPayment(code)).isInstanceOf(IllegalStateException.class);
        verify(payments,never()).save(any());
        verifyNoInteractions(publisher);
    }
    @Test void underpaidPayOSLinkCannotMarkPaymentPaid(){
        var payment=PaymentEntity.create(orderId,code,userId,"Customer","buyer@example.com",new BigDecimal("10000"),"VND");
        payment.attachPaymentLink("link-123","https://pay.payos.vn/web/link-123","qr");
        PaymentLink link=mock(PaymentLink.class);
        when(payments.findByOrderCode(code)).thenReturn(Optional.of(payment));
        when(gateway.getPaymentLink(code)).thenReturn(link);
        when(link.getOrderCode()).thenReturn(code);
        when(link.getId()).thenReturn("link-123");
        when(link.getAmount()).thenReturn(10000L);
        when(link.getAmountPaid()).thenReturn(5000L);
        when(link.getStatus()).thenReturn(PaymentLinkStatus.PAID);

        assertThatThrownBy(()->service.verifyPayOSPayment(code)).isInstanceOf(IllegalStateException.class);
        verify(payments,never()).save(any());
        verifyNoInteractions(publisher);
    }
    @Test void cancelledPayOSLinkMarksPaymentCancelled(){
        var payment=PaymentEntity.create(orderId,code,userId,"Customer","buyer@example.com",new BigDecimal("10000"),"VND");
        payment.attachPaymentLink("link-123","https://pay.payos.vn/web/link-123","qr");
        PaymentLink link=mock(PaymentLink.class);
        when(payments.findByOrderCode(code)).thenReturn(Optional.of(payment));
        when(gateway.getPaymentLink(code)).thenReturn(link);
        when(link.getOrderCode()).thenReturn(code);
        when(link.getId()).thenReturn("link-123");
        when(link.getAmount()).thenReturn(10000L);
        when(link.getStatus()).thenReturn(PaymentLinkStatus.CANCELLED);

        assertThat(service.verifyPayOSPayment(code).status()).isEqualTo(PaymentStatus.CANCELLED);
        verify(payments).save(payment);
        verify(publisher).publishCancelled(payment);
    }
}
