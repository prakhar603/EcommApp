package com.prakhar.ecomm.ecommbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id ;
    @Column(unique = true)
    private Integer productID ;
    private String name ;
    private Integer quantity ;
    private BigDecimal price ;

}
