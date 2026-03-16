package com.prakhar.ecomm.ecommbackend.repository;

import com.prakhar.ecomm.ecommbackend.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Products, Integer> {

    @Query("SELECT p FROM Products p WHERE p.productID = :productID")
    Optional<Products> getProductByProductID(@Param("productID") int productID);

    Optional<Products> findByProductID(int productId);
}
