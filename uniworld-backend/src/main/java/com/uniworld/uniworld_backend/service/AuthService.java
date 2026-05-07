package com.uniworld.uniworld_backend.service;

import com.uniworld.uniworld_backend.User;
import com.uniworld.uniworld_backend.dto.AuthRequest;
import com.uniworld.uniworld_backend.dto.AuthResponse;
import com.uniworld.uniworld_backend.dto.RegisterRequest;
import com.uniworld.uniworld_backend.repository.UserRepository;
import com.uniworld.uniworld_backend.security.JwtService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }

        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(Map.of("role", saved.getRole(), "userID", saved.getUserID()),
                org.springframework.security.core.userdetails.User
                        .withUsername(saved.getEmail())
                        .password(saved.getPassword())
                        .authorities("ROLE_" + saved.getRole())
                        .build());

        return new AuthResponse(token, saved.getUserID(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        if (request.identifier() == null || request.identifier().isBlank() || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email/username and password are required");
        }

        String identifier = request.identifier().trim();
        User user = userRepository.findByEmail(identifier.toLowerCase())
                .or(() -> userRepository.findByName(identifier))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getEmail(), request.password())
        );

        String token = jwtService.generateToken(Map.of("role", user.getRole(), "userID", user.getUserID()),
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities("ROLE_" + user.getRole())
                        .build());

        return new AuthResponse(token, user.getUserID(), user.getName(), user.getEmail(), user.getRole());
    }

    public AuthResponse currentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new AuthResponse(null, user.getUserID(), user.getName(), user.getEmail(), user.getRole());
    }
}
