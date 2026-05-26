package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.entity.Payment;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.repository.BookingRepository;
import com.suraj.MurtiSystem.repository.PaymentRepository;
import com.suraj.MurtiSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    public ApiResponse<List<Payment>> getPendingPayments() {
        List<Payment> payments = paymentRepository.findPendingPayments();
        return ApiResponse.success(payments);
    }

    public ApiResponse<Payment> submitOfflinePayment(String bookingId, Double amount, String transactionId, MultipartFile screenshot) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }

        Booking booking = bookingOpt.get();

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentType(Payment.PaymentType.ADVANCE);
        payment.setPaymentMethod(Payment.PaymentMethod.OFFLINE_UPI);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setTransactionId(transactionId);

        Payment saved = paymentRepository.save(payment);
        return ApiResponse.success(saved, "Payment submitted for verification");
    }

    public ApiResponse<Payment> verifyPayment(String paymentId, String status, String adminId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        Optional<User> adminOpt = userRepository.findById(adminId);

        if (paymentOpt.isEmpty()) {
            return ApiResponse.error("Payment not found");
        }
        if (adminOpt.isEmpty()) {
            return ApiResponse.error("Admin not found");
        }

        Payment payment = paymentOpt.get();
        payment.setStatus(Payment.PaymentStatus.valueOf(status));
        payment.setVerifiedBy(adminOpt.get());
        payment.setVerifiedAt(LocalDateTime.now());

        if (status.equals("VERIFIED")) {
            Booking booking = payment.getBooking();
            booking.setAdvancePaid(booking.getAdvancePaid() + payment.getAmount());
            booking.setRemainingAmount(booking.getTotalAmount() - booking.getAdvancePaid());

            if (booking.getRemainingAmount() == 0) {
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
            }
            bookingRepository.save(booking);
        }

        Payment saved = paymentRepository.save(payment);
        return ApiResponse.success(saved, "Payment " + status.toLowerCase());
    }

    public ApiResponse<List<Payment>> getBookingPayments(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }

        List<Payment> payments = paymentRepository.findByBooking(bookingOpt.get());
        return ApiResponse.success(payments);
    }
}