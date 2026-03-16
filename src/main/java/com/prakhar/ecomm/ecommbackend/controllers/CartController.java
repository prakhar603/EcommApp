package com.prakhar.ecomm.ecommbackend.controllers;

import com.prakhar.ecomm.ecommbackend.dto.AddToCartRequest;
import com.prakhar.ecomm.ecommbackend.dto.CartResponse;
import com.prakhar.ecomm.ecommbackend.dto.RemoveFromCartRequest;
import com.prakhar.ecomm.ecommbackend.dto.UpdateCartRequest;
import com.prakhar.ecomm.ecommbackend.service.ICartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getName()));
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(Principal principal,
                                                   @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(principal.getName(), request));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<CartResponse> removeFromCart(Principal principal,
                                                        @Valid @RequestBody RemoveFromCartRequest request) {
        return ResponseEntity.ok(cartService.removeFromCart(principal.getName(), request));
    }

    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateCartItem(Principal principal,
                                                        @Valid @RequestBody UpdateCartRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(principal.getName(), request));
    }
}
