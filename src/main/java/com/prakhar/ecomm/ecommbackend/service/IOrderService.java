package com.prakhar.ecomm.ecommbackend.service;

import com.prakhar.ecomm.ecommbackend.dto.OrderResponse;

public interface IOrderService {

    OrderResponse checkout(String userEmail);
}
