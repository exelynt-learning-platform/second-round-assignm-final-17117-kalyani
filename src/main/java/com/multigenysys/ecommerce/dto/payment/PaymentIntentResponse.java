package com.multigenysys.ecommerce.dto.payment;

public record PaymentIntentResponse(
        Long orderId,
        String paymentIntentId,
        String clientSecret,
        String status
) {
}
