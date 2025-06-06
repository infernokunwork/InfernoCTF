package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.models.ApiResponse;
import com.infernokun.infernoctf.models.entities.RefreshToken;
import com.infernokun.infernoctf.models.dto.LoginResponseDTO;
import com.infernokun.infernoctf.models.dto.RegistrationDTO;
import com.infernokun.infernoctf.services.AuthenticationService;
import com.infernokun.infernoctf.services.RefreshTokenService;
import com.infernokun.infernoctf.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationController(AuthenticationService authenticationService, UserService userService,
                                    RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<ApiResponse<Boolean>> registerUser(@RequestBody RegistrationDTO registrationDTO) {
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(HttpStatus.OK.value())
                .message("User registered successfully.")
                .data(authenticationService.registerUser(registrationDTO))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginUser(@RequestBody RegistrationDTO user) {
        LoginResponseDTO loginResponseDTO = authenticationService.loginUser(
                user.getUsername(), user.getPassword());

        return ResponseEntity.ok(ApiResponse.<LoginResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .message("Login successful..")
                .data(loginResponseDTO)
                .build());
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> revalidateToken(@RequestBody String token) {
        return ResponseEntity.ok(ApiResponse.<LoginResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .message("Token revalidated")
                .data(authenticationService.revalidateToken(token))
                .build());
    }

    @PostMapping("/token/check")
    public ResponseEntity<ApiResponse<Boolean>> checkToken(@RequestBody String token) {
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(HttpStatus.OK.value())
                .message("Token revalidated")
                .data(refreshTokenService.findByToken(token) != null)
                .build());
    }

    @DeleteMapping("/token/logout/{id}")
    public ResponseEntity<?> logoutUser(@PathVariable String id) {
        Optional<RefreshToken> refreshTokenOptional = refreshTokenService.deleteToken(id);

        if (refreshTokenOptional.isPresent()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok("Something crazy....");
        }
    }
}