package com.prakhar.ecomm.ecommbackend.controllers;

import com.prakhar.ecomm.ecommbackend.entity.Users;
import com.prakhar.ecomm.ecommbackend.service.IUserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    IUserService userService;

    @PostMapping("/create-user")
    public Users createUser(@RequestBody @NonNull Users user) {
        return userService.createUser(user);
    }
}
