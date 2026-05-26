package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.Payment;
import com.suraj.MurtiSystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByBooking(Booking booking);
    List<Payment> findByStatus(Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' ORDER BY p.createdAt ASC")
    List<Payment> findPendingPayments();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.booking = :booking AND p.status = 'VERIFIED'")
    Double getTotalPaidAmountByBooking(@Param("booking") Booking booking);
}