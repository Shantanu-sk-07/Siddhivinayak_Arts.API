package com.suraj.MurtiSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be 10 digits")
    private String phone;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Fields from CustomerUpdateRequestDto (optional for updates)
    private Boolean isActive;
}