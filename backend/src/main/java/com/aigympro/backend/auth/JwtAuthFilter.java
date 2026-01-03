package com.aigympro.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtAuthFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var claims = jwt.parse(token).getBody();
                UUID userId = UUID.fromString(claims.getSubject());
                @SuppressWarnings("unchecked")
                var roles = (List<String>) claims.get("roles");
                var authorities = roles==null ? List.<SimpleGrantedAuthority>of()
                        : roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_"+r)).toList();

                var authentication = new AbstractAuthenticationToken(authorities) {
                    @Override public Object getCredentials() { return token; }
                    @Override public Object getPrincipal() { return userId; }
                    @Override public boolean isAuthenticated() { return true; }
                };
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
}
