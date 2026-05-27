package com.suraj.MurtiSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentUpdateRequestDto {

    @NotBlank(message = "Status is required")
    private String status;
}