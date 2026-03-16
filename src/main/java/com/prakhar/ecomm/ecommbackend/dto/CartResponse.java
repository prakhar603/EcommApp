package com.prakhar.ecomm.ecommbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private Integer cartId;
    private Integer userId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
}
