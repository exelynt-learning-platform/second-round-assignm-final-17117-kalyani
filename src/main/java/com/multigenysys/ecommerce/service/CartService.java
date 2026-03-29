package com.multigenysys.ecommerce.service;

import com.multigenysys.ecommerce.dto.cart.CartItemRequest;
import com.multigenysys.ecommerce.dto.cart.CartItemResponse;
import com.multigenysys.ecommerce.dto.cart.CartResponse;
import com.multigenysys.ecommerce.dto.cart.UpdateCartItemRequest;
import com.multigenysys.ecommerce.entity.CartItem;
import com.multigenysys.ecommerce.entity.Product;
import com.multigenysys.ecommerce.entity.User;
import com.multigenysys.ecommerce.exception.BadRequestException;
import com.multigenysys.ecommerce.exception.ResourceNotFoundException;
import com.multigenysys.ecommerce.repository.CartItemRepository;
import com.multigenysys.ecommerce.repository.ProductRepository;
import com.multigenysys.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartResponse addItem(String email, CartItemRequest request) {
        User user = getUserByEmail(email);
        Product product = getProductById(request.productId());
        if (product.getStockQuantity() < request.quantity()) {
            throw new BadRequestException("Requested quantity is not available in stock");
        }
        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElseGet(CartItem::new);
        item.setUser(user);
        item.setProduct(product);
        int newQty = (item.getQuantity() == null ? 0 : item.getQuantity()) + request.quantity();
        if (newQty > product.getStockQuantity()) {
            throw new BadRequestException("Cart quantity cannot exceed stock quantity");
        }
        item.setQuantity(newQty);
        cartItemRepository.save(item);
        return getMyCart(email);
    }

    @Transactional
    public CartResponse updateItem(String email, Long cartItemId, UpdateCartItemRequest request) {
        User user = getUserByEmail(email);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only update your own cart");
        }
        if (request.quantity() > item.getProduct().getStockQuantity()) {
            throw new BadRequestException("Requested quantity exceeds stock quantity");
        }
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        return getMyCart(email);
    }

    @Transactional
    public CartResponse removeItem(String email, Long cartItemId) {
        User user = getUserByEmail(email);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only delete your own cart item");
        }
        cartItemRepository.delete(item);
        return getMyCart(email);
    }

    public CartResponse getMyCart(String email) {
        User user = getUserByEmail(email);
        List<CartItemResponse> items = cartItemRepository.findByUserId(user.getId()).stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, total);
    }

    public List<CartItem> getCartItemsForUser(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                subtotal
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}
