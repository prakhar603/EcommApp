package com.prakhar.ecomm.ecommbackend.service.impl;

import com.prakhar.ecomm.ecommbackend.dto.AddToCartRequest;
import com.prakhar.ecomm.ecommbackend.dto.CartItemResponse;
import com.prakhar.ecomm.ecommbackend.dto.CartResponse;
import com.prakhar.ecomm.ecommbackend.dto.RemoveFromCartRequest;
import com.prakhar.ecomm.ecommbackend.dto.UpdateCartRequest;
import com.prakhar.ecomm.ecommbackend.entity.Cart;
import com.prakhar.ecomm.ecommbackend.entity.CartItem;
import com.prakhar.ecomm.ecommbackend.entity.Products;
import com.prakhar.ecomm.ecommbackend.entity.Users;
import com.prakhar.ecomm.ecommbackend.exception.BadRequestException;
import com.prakhar.ecomm.ecommbackend.exception.ResourceNotFoundException;
import com.prakhar.ecomm.ecommbackend.repository.CartItemRepository;
import com.prakhar.ecomm.ecommbackend.repository.CartRepository;
import com.prakhar.ecomm.ecommbackend.repository.ProductRepository;
import com.prakhar.ecomm.ecommbackend.repository.UserRepository;
import com.prakhar.ecomm.ecommbackend.service.ICartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements ICartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(UserRepository userRepository,
                           CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        Cart cart = resolveCartForUser(userEmail);
        return toResponse(cart);
    }

    @Override
    public CartResponse addToCart(String userEmail, AddToCartRequest request) {
        Cart cart = resolveCartForUser(userEmail);
        Products product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Optional<CartItem> existing = cartItemRepository.findByCartAndProduct(cart, product);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (product.getQuantity() < newQty) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getQuantity());
            }
            item.setQuantity(newQty);
        } else {
            if (product.getQuantity() < request.getQuantity()) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getQuantity());
            }
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Override
    public CartResponse removeFromCart(String userEmail, RemoveFromCartRequest request) {
        Cart cart = resolveCartForUser(userEmail);
        CartItem item = cartItemRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + request.getCartItemId()));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to the current user");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toResponse(cart);
    }

    @Override
    public CartResponse updateCartItem(String userEmail, UpdateCartRequest request) {
        Cart cart = resolveCartForUser(userEmail);
        CartItem item = cartItemRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + request.getCartItemId()));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to the current user");
        }

        Products product = item.getProduct();
        if (product.getQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getQuantity());
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    private Cart resolveCartForUser(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .totalAmount(total)
                .build();
    }
}
