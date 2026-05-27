package com.suraj.MurtiSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PaymentRequestDto {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String transactionId;

    private MultipartFile screenshot;

    // Field from PaymentUpdateRequestDto
    @NotBlank(message = "Status is required")
    private String status;
}