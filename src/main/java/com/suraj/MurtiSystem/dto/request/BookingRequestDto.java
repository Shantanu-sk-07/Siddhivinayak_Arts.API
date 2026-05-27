package com.suraj.MurtiSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingRequestDto {

    @NotBlank(message = "Ganpati ID is required")
    private String ganpatiId;

    private Double advancePaid = 0.0;

    // Status field from BookingUpdateRequestDto
    @NotBlank(message = "Status is required")
    private String status;
}