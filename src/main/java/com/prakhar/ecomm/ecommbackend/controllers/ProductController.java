package com.prakhar.ecomm.ecommbackend.controllers;

import com.prakhar.ecomm.ecommbackend.entity.Products;
import com.prakhar.ecomm.ecommbackend.service.IProductService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    IProductService productService ;

    @PostMapping("/create-product")
    public Products createProduct(@RequestBody @NonNull Products product) {
        return productService.createProduct(product);
    }

    @PostMapping("/create-product/bulk")
    public ResponseEntity<List<Products>> createProductBulk (@RequestBody @NonNull List<Products> products){
        return new ResponseEntity<>(productService.createProductBulk(products), HttpStatus.CREATED);
    }

}
