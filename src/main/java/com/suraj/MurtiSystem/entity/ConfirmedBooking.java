package com.suraj.MurtiSystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "confirmed_bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmedBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String customerVillage;
    private String customerTaluka;
    private String customerDistrict;

    private String mandalName;

    @ElementCollection
    @CollectionTable(name = "booking_contacts", joinColumns = @JoinColumn(name = "booking_id"))
    private List<BookingContact> additionalContacts = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "ganpati_id", nullable = false)
    private Ganpati ganpati;

    @Column(nullable = false)
    private Double advancePayment;

    @Column(nullable = false)
    private Double remainingPayment;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private Double totalPaidSoFar = 0.0;

    @ElementCollection
    @CollectionTable(name = "booking_payment_history", joinColumns = @JoinColumn(name = "booking_id"))
    private List<PaymentRecord> paymentHistory = new ArrayList<>();

    private String bookingDate;
    private String actualPickupDate;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private String status;

    private String receiptNumber;

    private Boolean receiptSent = false;
    private LocalDateTime receiptSentAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingContact {
        private String name;
        private String phone;
        private String designation;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRecord {
        private Double amount;
        private LocalDateTime paymentDate;
        private String paymentType;
        private String notes;
        private Double remainingAfterPayment;
    }
}