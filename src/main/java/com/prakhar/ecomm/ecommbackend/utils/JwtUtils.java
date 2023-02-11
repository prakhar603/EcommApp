package com.prakhar.ecomm.ecommbackend.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;


@Component
public class JwtUtils {

    @Value("${jwt.expiry.time}")
    String expiryTime;
    @Value("${jwt.expiry.unit}")
    String expiryUnit;

    public String generateJwtToken(String email){
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(Integer.parseInt(expiryTime), ChronoUnit.valueOf(expiryUnit))))
                .compact();
        return token;
    }
}
