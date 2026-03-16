package com.prakhar.ecomm.ecommbackend.service;

import com.prakhar.ecomm.ecommbackend.dto.ProductRequest;
import com.prakhar.ecomm.ecommbackend.dto.ProductResponse;

import java.util.List;

public interface IProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Integer id);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(Integer id, ProductRequest request);

    void deleteProduct(Integer id);
}
