package com.multigenysys.ecommerce.service;

import com.multigenysys.ecommerce.dto.order.CreateOrderRequest;
import com.multigenysys.ecommerce.dto.order.OrderResponse;
import com.multigenysys.ecommerce.dto.order.ShippingDetailsRequest;
import com.multigenysys.ecommerce.entity.CartItem;
import com.multigenysys.ecommerce.entity.Order;
import com.multigenysys.ecommerce.entity.Product;
import com.multigenysys.ecommerce.entity.User;
import com.multigenysys.ecommerce.repository.OrderRepository;
import com.multigenysys.ecommerce.repository.ProductRepository;
import com.multigenysys.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_ShouldCreateOrderAndReduceStock() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@mail.com");

        Product product = new Product();
        product.setId(10L);
        product.setName("Phone");
        product.setPrice(new BigDecimal("500"));
        product.setStockQuantity(5);

        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(cartService.getCartItemsForUser(1L)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                new ShippingDetailsRequest("Street 1", "City", "State", "10001")
        );

        OrderResponse response = orderService.createOrder("user@mail.com", request);

        assertEquals(99L, response.orderId());
        assertEquals(new BigDecimal("1000"), response.totalPrice());
        assertEquals(3, product.getStockQuantity());
        verify(cartService).clearCart(1L);
    }
}
