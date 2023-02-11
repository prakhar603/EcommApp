package com.prakhar.ecomm.ecommbackend.service.impl;

import com.prakhar.ecomm.ecommbackend.entity.Users;
import com.prakhar.ecomm.ecommbackend.repository.UserRepository;
import com.prakhar.ecomm.ecommbackend.service.IUserService;
import com.prakhar.ecomm.ecommbackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Override
    public Users createUser(Users user){
        this.isEmailUnique(user.getEmail());
        user.setPassword(this.encrypt(user.getPassword()));
        return userRepository.save(user);
    }

    private String encrypt(String str){
        return new String(Base64.getEncoder().encode(str.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public void isEmailUnique(String email) {
        Users userByEmail = userRepository.getUserByEmail(email) ;
        if (userByEmail!=null){
            throw new RuntimeException("email id not unique.");
        }
    }

    @Override
    public String login(String username, String password) throws Exception {
        Users user = this.userRepository.getUserByEmail(username);
        System.out.println("password stored: "+user.getPassword());
        String passwordEncrypted = this.encrypt(password);
        System.out.println("password encrypted: "+passwordEncrypted);
        if (passwordEncrypted.equals(user.getPassword())){
            return jwtUtils.generateJwtToken(username);
        }
        throw new Exception("Username and password do not match");
    }
}
