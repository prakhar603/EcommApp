package com.prakhar.ecomm.ecommbackend.controllers;

import com.prakhar.ecomm.ecommbackend.entity.Users;
import com.prakhar.ecomm.ecommbackend.models.LoginRequest;
import com.prakhar.ecomm.ecommbackend.service.IUserService;
import com.prakhar.ecomm.ecommbackend.service.impl.UserServiceImpl;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    IUserService userService;

    @PostMapping("/create-user")
    public Users createUser(@RequestBody @NonNull Users user) {
        return userService.createUser(user);
    }

    @GetMapping("/login")
    public String login(@RequestBody @NonNull LoginRequest request) throws Exception {
        return this.userService.login(request.getUsername(), request.getPassword());
    }


}
