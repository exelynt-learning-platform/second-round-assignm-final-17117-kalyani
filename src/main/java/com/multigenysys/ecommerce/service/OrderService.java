package com.multigenysys.ecommerce.service;

import com.multigenysys.ecommerce.dto.order.CreateOrderRequest;
import com.multigenysys.ecommerce.dto.order.OrderItemResponse;
import com.multigenysys.ecommerce.dto.order.OrderResponse;
import com.multigenysys.ecommerce.entity.CartItem;
import com.multigenysys.ecommerce.entity.Order;
import com.multigenysys.ecommerce.entity.OrderItem;
import com.multigenysys.ecommerce.entity.PaymentStatus;
import com.multigenysys.ecommerce.entity.Product;
import com.multigenysys.ecommerce.entity.User;
import com.multigenysys.ecommerce.exception.BadRequestException;
import com.multigenysys.ecommerce.exception.ResourceNotFoundException;
import com.multigenysys.ecommerce.repository.OrderRepository;
import com.multigenysys.ecommerce.repository.ProductRepository;
import com.multigenysys.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;

    public OrderService(UserRepository userRepository, ProductRepository productRepository,
                        OrderRepository orderRepository, CartService cartService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = getUser(email);
        List<CartItem> cartItems = cartService.getCartItemsForUser(user.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.shippingDetails().address());
        order.setShippingCity(request.shippingDetails().city());
        order.setShippingState(request.shippingDetails().state());
        order.setShippingPostalCode(request.shippingDetails().postalCode());
        order.setPaymentStatus(PaymentStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItems.add(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setItems(orderItems);
        order.setTotalPrice(total);
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(user.getId());
        return toResponse(savedOrder);
    }

    public List<OrderResponse> getMyOrders(String email) {
        User user = getUser(email);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(String email, Long orderId) {
        User user = getUser(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only view your own orders");
        }
        return toResponse(order);
    }

    public Order getOrderEntityForUser(String email, Long orderId) {
        User user = getUser(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only pay for your own orders");
        }
        return order;
    }

    @Transactional
    public void updatePaymentStatus(Order order, PaymentStatus status, String paymentReference) {
        order.setPaymentStatus(status);
        order.setPaymentReference(paymentReference);
        orderRepository.save(order);
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getOrderStatus().name(),
                order.getPaymentStatus().name(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPostalCode(),
                order.getPaymentReference(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
