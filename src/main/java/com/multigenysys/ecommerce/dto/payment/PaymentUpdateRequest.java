package com.multigenysys.ecommerce.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record PaymentUpdateRequest(
        @NotBlank String paymentIntentId,
        boolean success
) {
}
