package com.prakhar.ecomm.ecommbackend.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @NonNull
    private String email;
    @NonNull
    private String firstName;
    @NonNull
    private String password;
    @NonNull
    private Integer age;
    @NonNull
    private String gender;
}
