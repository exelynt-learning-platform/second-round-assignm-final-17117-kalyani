package com.multigenysys.ecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;

public record ShippingDetailsRequest(
        @NotBlank String address,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String postalCode
) {
}
