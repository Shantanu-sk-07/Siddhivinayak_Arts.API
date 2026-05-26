package com.suraj.MurtiSystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String bookingId;

    @ManyToOne
    @JoinColumn(name = "ganpati_id", nullable = false)
    private Ganpati ganpati;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Double advancePaid = 0.0;

    private Double remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private String qrCode;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    private LocalDateTime pickupDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (remainingAmount == null) {
            this.remainingAmount = this.totalAmount - this.advancePaid;
        }
        if (bookingId == null) {
            this.bookingId = "BK" + System.currentTimeMillis();
        }
        if (bookingDate == null) {
            this.bookingDate = LocalDateTime.now();
        }
    }

    public enum BookingStatus {
        PENDING_REQUEST, APPROVED, CONFIRMED, PICKUP_COMPLETED, REJECTED, CANCELLED
    }
}