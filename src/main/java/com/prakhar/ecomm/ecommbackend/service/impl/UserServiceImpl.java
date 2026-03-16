package com.prakhar.ecomm.ecommbackend.service.impl;

import com.prakhar.ecomm.ecommbackend.dto.RegisterRequest;
import com.prakhar.ecomm.ecommbackend.dto.UserResponse;
import com.prakhar.ecomm.ecommbackend.entity.Cart;
import com.prakhar.ecomm.ecommbackend.entity.Users;
import com.prakhar.ecomm.ecommbackend.exception.BadRequestException;
import com.prakhar.ecomm.ecommbackend.exception.ResourceNotFoundException;
import com.prakhar.ecomm.ecommbackend.repository.UserRepository;
import com.prakhar.ecomm.ecommbackend.service.IUserService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements IUserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    @Override
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        Cart cart = new Cart();
        Users user = Users.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .password(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .gender(request.getGender())
                .build();
        cart.setUser(user);
        user.setCart(cart);

        Users saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return toResponse(user);
    }

    private UserResponse toResponse(Users user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .age(user.getAge())
                .gender(user.getGender())
                .build();
    }
}
