package com.urlshortener.controller;

import com.urlshortener.dto.AuthResponseDTO;
import com.urlshortener.entity.User;
import com.urlshortener.service.JwtService;
import com.urlshortener.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        User saved = userService.register(user);

        String token = jwtService.generateToken(saved.getEmail(), saved.getId());

        return ResponseEntity.ok(
                new AuthResponseDTO(
                        saved.getId(),
                        saved.getEmail(),
                        saved.getFullName(),
                        token
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String password = request.get("password");

        User user = userService.login(email, password);

        String token = jwtService.generateToken(user.getEmail(), user.getId());

        return ResponseEntity.ok(
                new AuthResponseDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        token
                )
        );
    }
}