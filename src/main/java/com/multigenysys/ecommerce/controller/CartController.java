package com.multigenysys.ecommerce.controller;

import com.multigenysys.ecommerce.dto.cart.CartItemRequest;
import com.multigenysys.ecommerce.dto.cart.CartResponse;
import com.multigenysys.ecommerce.dto.cart.UpdateCartItemRequest;
import com.multigenysys.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getMyCart(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<CartResponse> addItem(Authentication authentication, @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(authentication.getName(), request));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartResponse> updateItem(Authentication authentication, @PathVariable Long cartItemId,
                                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(authentication.getName(), cartItemId, request));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(Authentication authentication, @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItem(authentication.getName(), cartItemId));
    }
}
