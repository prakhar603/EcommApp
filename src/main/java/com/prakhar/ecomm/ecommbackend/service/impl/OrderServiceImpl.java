package com.prakhar.ecomm.ecommbackend.service.impl;

import com.prakhar.ecomm.ecommbackend.dto.OrderItemResponse;
import com.prakhar.ecomm.ecommbackend.dto.OrderResponse;
import com.prakhar.ecomm.ecommbackend.entity.Cart;
import com.prakhar.ecomm.ecommbackend.entity.CartItem;
import com.prakhar.ecomm.ecommbackend.entity.Order;
import com.prakhar.ecomm.ecommbackend.entity.OrderItem;
import com.prakhar.ecomm.ecommbackend.entity.Products;
import com.prakhar.ecomm.ecommbackend.entity.Users;
import com.prakhar.ecomm.ecommbackend.exception.BadRequestException;
import com.prakhar.ecomm.ecommbackend.exception.ResourceNotFoundException;
import com.prakhar.ecomm.ecommbackend.repository.CartRepository;
import com.prakhar.ecomm.ecommbackend.repository.OrderRepository;
import com.prakhar.ecomm.ecommbackend.repository.ProductRepository;
import com.prakhar.ecomm.ecommbackend.repository.UserRepository;
import com.prakhar.ecomm.ecommbackend.service.IOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements IOrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(UserRepository userRepository,
                            CartRepository cartRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public OrderResponse checkout(String userEmail) {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userEmail));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart");
        }

        // Validate all stock upfront before making any changes
        for (CartItem cartItem : cart.getItems()) {
            Products product = cartItem.getProduct();
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for '" + product.getName() + "'. Available: " + product.getQuantity()
                );
            }
        }

        Order order = Order.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Products product = cartItem.getProduct();

            // Deduct stock
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal itemPrice = product.getPrice();
            total = total.add(itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(itemPrice)
                    .build();
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order creation
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .orderItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
