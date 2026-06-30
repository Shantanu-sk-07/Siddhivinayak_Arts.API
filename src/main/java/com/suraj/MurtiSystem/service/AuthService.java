package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.config.JwtTokenProvider;
import com.suraj.MurtiSystem.dto.request.LoginRequest;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.LoginResponse;
import com.suraj.MurtiSystem.entity.Owner;
import com.suraj.MurtiSystem.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ApiResponse<LoginResponse> login(LoginRequest request) {
        try {
            Owner owner = ownerRepository.findByEmail(request.getEmail())
                    .orElse(null);

            if (owner == null) {
                return ApiResponse.error("Invalid email or password");
            }

            if (!passwordEncoder.matches(request.getPassword(), owner.getPassword())) {
                return ApiResponse.error("Invalid email or password");
            }

            if (!owner.getIsActive()) {
                return ApiResponse.error("Account is deactivated");
            }

            String token = jwtTokenProvider.generateToken(owner.getEmail());

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(owner.getEmail(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            LoginResponse response = new LoginResponse(
                    owner.getId(),
                    owner.getName(),
                    owner.getEmail(),
                    owner.getPhone(),
                    owner.getRole(),
                    token
            );

            return ApiResponse.success(response, "Login successful");
        } catch (Exception e) {
            return ApiResponse.error("Invalid email or password");
        }
    }
}