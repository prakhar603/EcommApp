package com.prakhar.ecomm.ecommbackend.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import lombok.Data;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;


public class JwtUtils {

    public static String generateJwtToken(String email){
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(20, ChronoUnit.MINUTES)))
                .compact();
        return token;
    }
}
