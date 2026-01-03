package com.aigympro.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    @Value("${app.jwt.secret:YWlnLWd5bS1wcm8tMTIzNDU2Nzg5MC1zZWNyZXQta2V5LXNwcmluZw==}")
    private String secretBase64;

    @Value("${app.jwt.access-ttl-minutes:60}")
    private long accessTtlMinutes;



    private SecretKey key;

    @PostConstruct
    void init() {
        // декодируем Base64 и создаём HMAC ключ
        byte[] bytes = Decoders.BASE64.decode(secretBase64);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccess(UUID userId, String email, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + TimeUnit.MINUTES.toMillis(accessTtlMinutes));

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("email", email)
                .claim("roles", roles)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public UUID userId(String token) {
        return UUID.fromString(parse(token).getBody().getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(String token) {
        return (List<String>) parse(token).getBody().get("roles");
    }
}
