package com.prakhar.ecomm.ecommbackend.service.impl;

import com.prakhar.ecomm.ecommbackend.entity.Users;
import com.prakhar.ecomm.ecommbackend.repository.UserRepository;
import com.prakhar.ecomm.ecommbackend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public Users createUser(Users user){
        user.setPassword(this.encrypt(user.getPassword()));
        return userRepository.save(user);
    }

    private String encrypt(String str){
        return Arrays.toString(Base64.getEncoder().encode(str.getBytes(StandardCharsets.UTF_8)));
    }
}
