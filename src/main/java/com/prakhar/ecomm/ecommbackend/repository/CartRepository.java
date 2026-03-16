package com.prakhar.ecomm.ecommbackend.repository;

import com.prakhar.ecomm.ecommbackend.entity.Cart;
import com.prakhar.ecomm.ecommbackend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByUser(Users user);
}
