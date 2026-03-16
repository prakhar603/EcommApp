package com.prakhar.ecomm.ecommbackend.repository;

import com.prakhar.ecomm.ecommbackend.entity.Order;
import com.prakhar.ecomm.ecommbackend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUser(Users user);
}
