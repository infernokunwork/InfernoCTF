package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;
    private final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);
    private final UserService userService;

    public TokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, RefreshTokenService refreshTokenService, UserService userService) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    public String generateJwt(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(10, ChronoUnit.MINUTES); // Extended expiration time

        String scope = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(user.getId())
                .claim("roles", scope)
                .build();

        String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        refreshTokenService.createRefreshToken(user.getUsername(), token, expiration);
        return token;
    }

    @Bean
    private boolean applicationJWT() {
        List<User> users = this.userService.findAllUsers();

        User admin = users.stream()
                        .filter(user -> "admin".equals(user.getUsername()))
                        .findFirst()
                .orElse(new User("admin", "defaultPassword"));

        String scope = admin.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(365, ChronoUnit.DAYS))
                .subject("admin")
                .claim("roles", scope)
                .build();

        LOGGER.info("TOKEN: {}", this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue());
        return true;
    }
}
