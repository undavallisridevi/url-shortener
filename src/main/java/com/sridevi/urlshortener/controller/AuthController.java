package com.sridevi.urlshortener.controller;

import com.sridevi.urlshortener.dto.*;
import com.sridevi.urlshortener.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }
    @PostMapping("/register") ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request)); }
    @PostMapping("/login") AuthResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }
}
