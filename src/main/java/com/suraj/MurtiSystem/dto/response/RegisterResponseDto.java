package com.suraj.MurtiSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponseDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String message;
}