package com.suraj.MurtiSystem.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReceiptResponseDto {
    private String id;
    private String token;
    private String receiptUrl;
    private String bookingId;
    private String pdfPath;
    private LocalDateTime createdDate;
    private Boolean isActive;
}