package com.shailyverma.feasto.auth_users.controller;


import com.shailyverma.feasto.auth_users.dtos.LoginRequest;
import com.shailyverma.feasto.auth_users.dtos.RegistrationRequest;
import com.shailyverma.feasto.auth_users.services.AuthService;
import com.shailyverma.feasto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
    @RequestMapping("/api/auth")
    @RequiredArgsConstructor
    public class AuthController {

        private final AuthService authService;

        @PostMapping("/register")
        public ResponseEntity<Response<?>> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
            return ResponseEntity.ok(authService.registration(registrationRequest));
        }

        @PostMapping("/login")
        public ResponseEntity<Response<?>> login(@Valid @RequestBody LoginRequest loginRequest) {
            return ResponseEntity.ok(authService.login(loginRequest));
        }
    }
