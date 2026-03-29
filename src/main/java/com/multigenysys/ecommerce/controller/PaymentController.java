package com.multigenysys.ecommerce.controller;

import com.multigenysys.ecommerce.dto.payment.PaymentIntentResponse;
import com.multigenysys.ecommerce.dto.payment.PaymentUpdateRequest;
import com.multigenysys.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}/intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(Authentication authentication,
                                                                     @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(authentication.getName(), orderId));
    }

    @PostMapping("/orders/{orderId}/result")
    public ResponseEntity<Map<String, String>> updatePaymentResult(Authentication authentication,
                                                                   @PathVariable Long orderId,
                                                                   @Valid @RequestBody PaymentUpdateRequest request) {
        paymentService.updatePaymentResult(authentication.getName(), orderId, request);
        return ResponseEntity.ok(Map.of("message", "Payment status updated"));
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<Map<String, String>> stripeWebhook(@RequestBody String payload) {
        return ResponseEntity.ok(Map.of("message", "Webhook endpoint ready", "payloadLength", String.valueOf(payload.length())));
    }
}
