package com.suraj.MurtiSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponseDto {
    private String id;
    private String bookingId;
    private String ganpatiId;
    private String ganpatiName;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private Double totalAmount;
    private Double advancePaid;
    private Double remainingAmount;
    private String status;
    private String qrCode;
    private LocalDateTime bookingDate;
    private LocalDateTime pickupDate;
    private LocalDateTime createdAt;
}