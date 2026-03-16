package com.prakhar.ecomm.ecommbackend.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UpdateCartRequest {

    @NotNull(message = "Cart item ID is required")
    private Integer cartItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
