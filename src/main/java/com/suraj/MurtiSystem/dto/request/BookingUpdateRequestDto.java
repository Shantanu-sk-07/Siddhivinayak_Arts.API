package com.suraj.MurtiSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingUpdateRequestDto {

    @NotBlank(message = "Status is required")
    private String status;

    private Double advancePaid;
}