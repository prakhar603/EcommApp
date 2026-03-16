package com.prakhar.ecomm.ecommbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Integer id;
    private String email;
    private String firstName;
    private Integer age;
    private String gender;
}
