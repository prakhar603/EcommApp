package com.prakhar.ecomm.ecommbackend.repository;

import com.prakhar.ecomm.ecommbackend.entity.Cart;
import com.prakhar.ecomm.ecommbackend.entity.CartItem;
import com.prakhar.ecomm.ecommbackend.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Products product);
}
