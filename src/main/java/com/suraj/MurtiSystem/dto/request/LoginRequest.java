package com.suraj.MurtiSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "ईमेल आवश्यक आहे")
    private String email;

    @NotBlank(message = "पासवर्ड आवश्यक आहे")
    private String password;
}