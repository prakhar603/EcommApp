package com.prakhar.ecomm.ecommbackend.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RemoveFromCartRequest {

    @NotNull(message = "Cart item ID is required")
    private Integer cartItemId;
}
