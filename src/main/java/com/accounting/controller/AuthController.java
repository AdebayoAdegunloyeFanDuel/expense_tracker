package com.accounting.controller;

import com.accounting.dto.AuthResponse;
import com.accounting.dto.LoginRequest;
import com.accounting.dto.RegisterRequest;
import com.accounting.dto.UserDto;
import com.accounting.security.JwtTokenProvider;
import com.accounting.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email={}", request.getEmail());

        UserDto userDto = userService.registerUser(request);
        String token = jwtTokenProvider.generateToken(userDto.getId(), userDto.getEmail());

        AuthResponse response = AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .user(userDto)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email={}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDto userDto = userService.getUserProfile(
            Long.valueOf(authentication.getPrincipal().toString())
        );

        String token = jwtTokenProvider.generateToken(userDto.getId(), userDto.getEmail());

        AuthResponse response = AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .user(userDto)
            .build();

        return ResponseEntity.ok(response);
    }
}
