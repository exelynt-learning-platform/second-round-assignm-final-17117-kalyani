package com.multigenysys.ecommerce.service;

import com.multigenysys.ecommerce.dto.payment.PaymentIntentResponse;
import com.multigenysys.ecommerce.dto.payment.PaymentUpdateRequest;
import com.multigenysys.ecommerce.entity.Order;
import com.multigenysys.ecommerce.entity.PaymentStatus;
import com.multigenysys.ecommerce.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPaymentIntent_ShouldReturnMockIntentWhenSecretMissing() {
        Order order = new Order();
        order.setId(1L);
        order.setTotalPrice(new BigDecimal("250"));
        order.setPaymentStatus(PaymentStatus.PENDING);

        ReflectionTestUtils.setField(paymentService, "stripeSecretKey", "sk_test_replace_me");
        when(orderService.getOrderEntityForUser("user@mail.com", 1L)).thenReturn(order);

        PaymentIntentResponse response = paymentService.createPaymentIntent("user@mail.com", 1L);

        assertEquals(1L, response.orderId());
        assertEquals("MOCK_PENDING", response.status());
    }

    @Test
    void updatePaymentResult_ShouldMarkSuccess() {
        Order order = new Order();
        order.setId(1L);
        order.setPaymentReference("pi_123");
        when(orderService.getOrderEntityForUser("user@mail.com", 1L)).thenReturn(order);

        ReflectionTestUtils.setField(paymentService, "stripeSecretKey", "");
        paymentService.updatePaymentResult("user@mail.com", 1L, new PaymentUpdateRequest("pi_123", true));

        verify(orderService).updatePaymentStatus(order, PaymentStatus.SUCCESS, "pi_123");
    }

    @Test
    void updatePaymentResult_ShouldRejectMismatchedPaymentIntent() {
        Order order = new Order();
        order.setId(1L);
        order.setPaymentReference("pi_expected");
        when(orderService.getOrderEntityForUser("user@mail.com", 1L)).thenReturn(order);

        assertThrows(BadRequestException.class,
                () -> paymentService.updatePaymentResult("user@mail.com", 1L, new PaymentUpdateRequest("pi_other", true)));
    }
}
