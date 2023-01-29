package com.prakhar.ecomm.ecommbackend.service;

import com.prakhar.ecomm.ecommbackend.entity.Users;

public interface IUserService {
    Users createUser(Users user);

    String login(String username, String password) throws Exception;
}

/*
* completed: user registration
* todo
*  login flow -> username and pswd (i/p)
* return JWT token
* API for authorization (custom annotation)
*
* 2 Services - USer and Product
*
* User ->
* 1. Registration of user (name, password, username, age) : COMPLETED
* 2. Logging in of the user using Username and pswd
* 3. return JWT authentication
*
* OTP at
*
*
* 2 User tables to store data
* Table 1 ->  User details (FirstName, Age, Gender)
* Table 2 -> Login credentials (Email, Password, Last login time, created on)
*
*
*
*
*
* */
