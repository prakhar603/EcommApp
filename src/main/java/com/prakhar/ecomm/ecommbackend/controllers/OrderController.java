package com.prakhar.ecomm.ecommbackend.controllers;

import com.prakhar.ecomm.ecommbackend.dto.OrderResponse;
import com.prakhar.ecomm.ecommbackend.service.IOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(Principal principal) {
        return new ResponseEntity<>(orderService.checkout(principal.getName()), HttpStatus.CREATED);
    }
}
