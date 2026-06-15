package com.sridevi.urlshortener.service.impl;

import com.sridevi.urlshortener.dto.*;
import com.sridevi.urlshortener.entity.User;
import com.sridevi.urlshortener.exception.ConflictException;
import com.sridevi.urlshortener.repository.UserRepository;
import com.sridevi.urlshortener.security.JwtService;
import com.sridevi.urlshortener.service.AuthService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository users; private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager; private final JwtService jwtService;
    public AuthServiceImpl(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users; this.encoder = encoder; this.authenticationManager = authenticationManager; this.jwtService = jwtService;
    }
    @Override @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByUsername(request.username())) throw new ConflictException("Username is already registered");
        User user = new User(); user.setUsername(request.username()); user.setPassword(encoder.encode(request.password()));
        try { users.saveAndFlush(user); }
        catch (DataIntegrityViolationException ex) { throw new ConflictException("Username is already registered"); }
        return token(request.username());
    }
    @Override public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return token(request.username());
    }
    private AuthResponse token(String username) { return new AuthResponse(jwtService.generateToken(username), "Bearer", jwtService.expirationSeconds()); }
}
