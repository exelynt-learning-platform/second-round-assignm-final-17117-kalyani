package com.multigenysys.ecommerce.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        BigDecimal totalPrice,
        String orderStatus,
        String paymentStatus,
        String shippingAddress,
        String shippingCity,
        String shippingState,
        String shippingPostalCode,
        String paymentReference,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
