package com.prakhar.ecomm.ecommbackend.service;

import com.prakhar.ecomm.ecommbackend.entity.Products;

import java.util.List;

public interface IProductService {

    Products createProduct(Products product) ;
    List<Products> createProductBulk(List<Products> products);
}
