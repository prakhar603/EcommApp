package com.prakhar.ecomm.ecommbackend.repository;

import com.prakhar.ecomm.ecommbackend.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Products, Integer> {

    @Query("SELECT u FROM Products u WHERE u.productID= :productID")
    public Products getProductByProductID(@Param("productID") int productID) ;

    public Products findByProductID(int productId);
}
