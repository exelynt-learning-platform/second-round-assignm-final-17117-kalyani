package com.multigenysys.ecommerce.service;

import com.multigenysys.ecommerce.dto.payment.PaymentIntentResponse;
import com.multigenysys.ecommerce.dto.payment.PaymentUpdateRequest;
import com.multigenysys.ecommerce.entity.Order;
import com.multigenysys.ecommerce.entity.PaymentStatus;
import com.multigenysys.ecommerce.exception.BadRequestException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private final OrderService orderService;

    @Value("${payment.stripe.secret-key}")
    private String stripeSecretKey;

    public PaymentService(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional
    public PaymentIntentResponse createPaymentIntent(String email, Long orderId) {
        Order order = orderService.getOrderEntityForUser(email, orderId);
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Order payment is already completed");
        }

        if (stripeSecretKey == null || stripeSecretKey.isBlank() || stripeSecretKey.contains("replace_me")) {
            String intentId = "pi_mock_" + UUID.randomUUID();
            String secret = "pi_mock_secret_" + UUID.randomUUID();
            orderService.updatePaymentStatus(order, PaymentStatus.PENDING, intentId);
            return new PaymentIntentResponse(order.getId(), intentId, secret, "MOCK_PENDING");
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(order.getTotalPrice().multiply(java.math.BigDecimal.valueOf(100)).longValue())
                    .setCurrency("usd")
                    .setDescription("Order #" + order.getId())
                    .putMetadata("orderId", String.valueOf(order.getId()))
                    .build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            orderService.updatePaymentStatus(order, PaymentStatus.PENDING, paymentIntent.getId());
            return new PaymentIntentResponse(order.getId(), paymentIntent.getId(), paymentIntent.getClientSecret(), paymentIntent.getStatus());
        } catch (StripeException e) {
            throw new BadRequestException("Payment gateway error: " + e.getMessage());
        }
    }

    @Transactional
    public void updatePaymentResult(String email, Long orderId, PaymentUpdateRequest request) {
        Order order = orderService.getOrderEntityForUser(email, orderId);
        if (request.success()) {
            orderService.updatePaymentStatus(order, PaymentStatus.SUCCESS, request.paymentIntentId());
        } else {
            orderService.updatePaymentStatus(order, PaymentStatus.FAILED, request.paymentIntentId());
        }
    }
}
