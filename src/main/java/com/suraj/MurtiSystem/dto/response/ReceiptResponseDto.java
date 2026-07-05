package com.suraj.MurtiSystem.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReceiptResponseDto {
    private String id;
    private String token;
    private String receiptUrl;
    private String bookingId;
    private String pdfPath;
    private LocalDateTime createdDate;
    private Boolean isActive;

    private String receiptNumber;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String customerTaluka;
    private String customerDistrict;
    private String mandalName;
    private String ganpatiName;
    private String ganpatiHeight;
    private Double ganpatiPrice;
    private Double advancePayment;
    private Double remainingPayment;
    private Double totalPrice;
    private Double totalPaidSoFar;
    private String bookingDate;
    private String status;
    private List<PaymentRecordDto> paymentHistory;

    @Data
    public static class PaymentRecordDto {
        private Double amount;
        private LocalDateTime paymentDate;
        private String paymentType;
        private String notes;
        private Double remainingAfterPayment;
    }
}