package com.suraj.MurtiSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponseDto {
    private String id;
    private String bookingId;
    private Double amount;
    private String paymentType;
    private String paymentMethod;
    private String status;
    private String transactionId;
    private String screenshot;
    private LocalDateTime createdAt;
}