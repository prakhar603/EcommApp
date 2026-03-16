package com.prakhar.ecomm.ecommbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String description;
}
