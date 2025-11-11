package com.example.bankcards.security;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;


    public String generateAccessToken(User user) {
        var roles = getRoles(user);

        return Jwts.builder()
                .subject(user.getPhoneNumber())
                .claim("roles", roles)
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSigningKey(accessSecret))
                .compact();
    }


    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getPhoneNumber())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(refreshSecret))
                .compact();
    }


    public Claims extractClaims(String token, boolean isRefresh) {
        String secret = isRefresh ? refreshSecret : accessSecret;

        return Jwts.parser()
                .verifyWith(getSigningKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public String extractPhoneNumber(String token, boolean isRefresh) {
        return extractClaims(token, isRefresh).getSubject();
    }


    public boolean isTokenValid(String token, boolean isRefresh, UserDetails userDetails) {
        final String phoneNumber = extractPhoneNumber(token, isRefresh);
        boolean isTokenExpired = extractClaims(token, isRefresh).getExpiration().before(new Date());

        return phoneNumber.equals(userDetails.getUsername()) && !isTokenExpired;
    }


    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, true, userDetails);
    }


    private SecretKey getSigningKey(String secret) {
        byte[] decodedKey = Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(decodedKey);
    }


    private List<String> getRoles(User user) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();
    }
}
