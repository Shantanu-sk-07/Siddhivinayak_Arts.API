package com.suraj.MurtiSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerUpdateRequestDto {

    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be 10 digits")
    private String phone;

    private Boolean isActive;
}