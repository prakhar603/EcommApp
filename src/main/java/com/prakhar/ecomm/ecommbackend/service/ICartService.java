package com.prakhar.ecomm.ecommbackend.service;

import com.prakhar.ecomm.ecommbackend.dto.AddToCartRequest;
import com.prakhar.ecomm.ecommbackend.dto.CartResponse;
import com.prakhar.ecomm.ecommbackend.dto.RemoveFromCartRequest;
import com.prakhar.ecomm.ecommbackend.dto.UpdateCartRequest;

public interface ICartService {

    CartResponse getCart(String userEmail);

    CartResponse addToCart(String userEmail, AddToCartRequest request);

    CartResponse removeFromCart(String userEmail, RemoveFromCartRequest request);

    CartResponse updateCartItem(String userEmail, UpdateCartRequest request);
}
