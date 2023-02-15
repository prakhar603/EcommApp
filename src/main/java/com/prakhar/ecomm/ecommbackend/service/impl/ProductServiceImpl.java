package com.prakhar.ecomm.ecommbackend.service.impl;

import com.prakhar.ecomm.ecommbackend.entity.Products;
import com.prakhar.ecomm.ecommbackend.repository.ProductRepository;
import com.prakhar.ecomm.ecommbackend.service.IProductService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductRepository productRepository;

    public Products createProduct(@NonNull Products product) {
        return productRepository.save(product) ;
    }

    @Override
    public List<Products> createProductBulk(List<Products> products) {
        List<Products> result = new ArrayList<>();
        for(Products p : products){
            result.add(this.createProduct(p));
        }
        return result;
    }

    public void isProductIDUnique(Integer productID) {

    }
}
