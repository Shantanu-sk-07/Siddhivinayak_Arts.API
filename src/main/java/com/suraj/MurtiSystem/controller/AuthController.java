package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.request.LoginRequest;
import com.suraj.MurtiSystem.dto.request.RegisterRequest;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.LoginResponse;
import com.suraj.MurtiSystem.dto.response.RegisterResponseDto;
import com.suraj.MurtiSystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponseDto> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}