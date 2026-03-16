package com.prakhar.ecomm.ecommbackend.service;

import com.prakhar.ecomm.ecommbackend.dto.RegisterRequest;
import com.prakhar.ecomm.ecommbackend.dto.UserResponse;

public interface IUserService {

    UserResponse registerUser(RegisterRequest request);

    UserResponse getUserProfile(String email);
}
