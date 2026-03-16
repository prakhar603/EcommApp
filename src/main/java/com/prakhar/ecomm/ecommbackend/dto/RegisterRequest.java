package com.prakhar.ecomm.ecommbackend.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be a positive number")
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender;
}
