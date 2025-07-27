package com.stefanjakic.fridge.controller;

import com.stefanjakic.fridge.entity.User;
import com.stefanjakic.fridge.service.UserService;
import com.stefanjakic.fridge.config.JwtService;
import com.stefanjakic.fridge.dto.CreateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String USER_REGISTERED_SUCCESS = "User registered successfully";
    private static final String INVALID_CREDENTIALS = "Invalid credentials";
    private static final String USER_NOT_FOUND = "User not found";

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            userService.register(user);
            return ResponseEntity.ok(USER_REGISTERED_SUCCESS);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

            if (!userService.validateCredentials(email, password)) {
                return ResponseEntity.badRequest().body(INVALID_CREDENTIALS);
            }

            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(Map.of("token", token, "role", user.getRole()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
