// ConfirmedBookingResponseDto.java
package com.suraj.MurtiSystem.dto.response;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfirmedBookingResponseDto {
    private String id;
    private String customerId;
    private CustomerResponseDto customer;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String customerTaluka;
    private String customerDistrict;
    private String mandalName;
    private List<BookingContactDto> additionalContacts;
    private GanpatiResponseDto ganpati;
    private String ganpatiId;
    private Double advancePayment;
    private Double remainingPayment;
    private Double totalPrice;
    private Double totalPaidSoFar;
    private List<PaymentRecordDto> paymentHistory;
    private String bookingDate;
    private String actualPickupDate;
    private String notes;
    private String status;
    private String receiptNumber;
    private Boolean receiptSent;
    private LocalDateTime receiptSentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class BookingContactDto {
        private String name;
        private String phone;
        private String designation;
    }

    @Data
    public static class CustomerResponseDto {
        private String id;
        private String name;
        private String phone;
        private String registrationType;
        private String mandalName;
        private String address;
        private String city;
        private String taluka;
        private String district;
        private String state;
        private String pincode;
        private List<ContactPersonDto> contactPersons;
    }

    @Data
    public static class ContactPersonDto {
        private String name;
        private String phone;
        private String designation;
    }

    @Data
    public static class PaymentRecordDto {
        private Double amount;
        private LocalDateTime paymentDate;
        private String paymentType;
        private String notes;
        private Double remainingAfterPayment;
    }
}