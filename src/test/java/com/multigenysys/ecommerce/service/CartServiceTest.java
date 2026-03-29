package com.multigenysys.ecommerce.service;

import com.multigenysys.ecommerce.dto.cart.CartItemRequest;
import com.multigenysys.ecommerce.dto.cart.CartResponse;
import com.multigenysys.ecommerce.entity.CartItem;
import com.multigenysys.ecommerce.entity.Product;
import com.multigenysys.ecommerce.entity.User;
import com.multigenysys.ecommerce.exception.BadRequestException;
import com.multigenysys.ecommerce.repository.CartItemRepository;
import com.multigenysys.ecommerce.repository.ProductRepository;
import com.multigenysys.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addItem_ShouldAddProductToCart() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@mail.com");

        Product product = new Product();
        product.setId(10L);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("1200"));
        product.setStockQuantity(5);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        CartResponse response = cartService.addItem("user@mail.com", new CartItemRequest(10L, 2));

        assertEquals(1, response.items().size());
        assertEquals(new BigDecimal("2400"), response.total());
    }

    @Test
    void addItem_ShouldFailForInsufficientStock() {
        User user = new User();
        user.setId(1L);
        Product product = new Product();
        product.setId(10L);
        product.setStockQuantity(1);

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> cartService.addItem("user@mail.com", new CartItemRequest(10L, 2)));
    }
}
