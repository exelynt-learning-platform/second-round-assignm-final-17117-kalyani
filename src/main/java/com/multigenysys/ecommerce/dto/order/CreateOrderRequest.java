package com.multigenysys.ecommerce.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull @Valid ShippingDetailsRequest shippingDetails
) {
}
