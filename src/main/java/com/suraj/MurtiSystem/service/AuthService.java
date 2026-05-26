package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.request.LoginRequest;
import com.suraj.MurtiSystem.dto.request.RegisterRequest;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.LoginResponse;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.repository.UserRepository;
import com.suraj.MurtiSystem.config.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public ApiResponse<LoginResponse> login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ApiResponse.error("Invalid email or password");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error("Invalid email or password");
        }

        if (!user.getIsActive()) {
            return ApiResponse.error("Account is deactivated. Please contact admin.");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                token
        );

        return ApiResponse.success(response, "Login successful");
    }

    public ApiResponse<User> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email already registered");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            return ApiResponse.error("Phone number already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.CUSTOMER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        savedUser.setPassword(null);

        return ApiResponse.success(savedUser, "Registration successful");
    }
}