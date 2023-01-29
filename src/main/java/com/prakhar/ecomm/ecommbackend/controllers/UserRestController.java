package com.prakhar.ecomm.ecommbackend.controllers;

import com.prakhar.ecomm.ecommbackend.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRestController {
    @Autowired
    private UserServiceImpl service ;

//    @PostMapping("/user/check_email")
//    public String checkDuplicateEmail(@Param("email") String email) {
//        return service.isEmailUnique(email) ? "OK" : "Already exists" ;
//    }

}
