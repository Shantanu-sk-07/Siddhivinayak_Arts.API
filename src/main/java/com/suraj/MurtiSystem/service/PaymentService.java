package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.PaymentResponseDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WhatsAppService whatsAppService;

    public ApiResponse<List<PaymentResponseDto>> getPendingPayments() {
        List<Payment> payments = paymentRepository.findPendingPayments();
        List<PaymentResponseDto> responseList = payments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ApiResponse.success(responseList);
    }

    public ApiResponse<List<PaymentResponseDto>> getCustomerPayments(String customerId) {
        Optional<User> customer = userRepository.findById(customerId);
        if (customer.isEmpty()) {
            return ApiResponse.success(new ArrayList<>());
        }
        List<Booking> bookings = bookingRepository.findByCustomer(customer.get());
        List<Payment> allPayments = new ArrayList<>();
        for (Booking booking : bookings) {
            allPayments.addAll(paymentRepository.findByBooking(booking));
        }
        List<PaymentResponseDto> responseList = allPayments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ApiResponse.success(responseList);
    }

    public ApiResponse<PaymentResponseDto> submitOfflinePayment(String bookingId, Double amount, String transactionId, MultipartFile screenshot) {
        try {
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
            if (screenshot != null && !screenshot.isEmpty()) {
                payment.setScreenshot("uploaded");
            }

            Payment saved = paymentRepository.save(payment);
            return ApiResponse.success(mapToResponseDto(saved), "Payment submitted for verification");
        } catch (Exception e) {
            return ApiResponse.error("Failed to submit payment: " + e.getMessage());
        }
    }

    public ApiResponse<PaymentResponseDto> verifyPayment(String paymentId, String status, String adminId) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            Optional<User> adminOpt = userRepository.findById(adminId);

            if (paymentOpt.isEmpty()) {
                return ApiResponse.error("Payment not found");
            }

            Payment payment = paymentOpt.get();
            payment.setStatus(Payment.PaymentStatus.valueOf(status));

            if (status.equals("VERIFIED") && adminOpt.isPresent()) {
                payment.setVerifiedBy(adminOpt.get());
                payment.setVerifiedAt(LocalDateTime.now());

                Booking booking = payment.getBooking();
                booking.setAdvancePaid(booking.getAdvancePaid() + payment.getAmount());
                booking.setRemainingAmount(booking.getTotalAmount() - booking.getAdvancePaid());

                if (booking.getRemainingAmount() <= 0) {
                    booking.setStatus(Booking.BookingStatus.CONFIRMED);

                    String message = whatsAppService.getPaymentReceivedMessage(
                            booking.getCustomer().getName(),
                            booking.getGanpati().getName(),
                            booking.getBookingId(),
                            payment.getAmount(),
                            payment.getPaymentMethod().name()
                    );
                    String whatsappLink = whatsAppService.generateWhatsAppLink(message);
                    System.out.println("📱 WhatsApp Admin Link (Payment): " + whatsappLink);
                }
                bookingRepository.save(booking);
            }

            Payment saved = paymentRepository.save(payment);
            return ApiResponse.success(mapToResponseDto(saved), "Payment " + status.toLowerCase());
        } catch (Exception e) {
            return ApiResponse.error("Failed to verify payment: " + e.getMessage());
        }
    }

    public ApiResponse<List<PaymentResponseDto>> getBookingPayments(String bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return ApiResponse.success(new ArrayList<>());
            }

            List<Payment> payments = paymentRepository.findByBooking(bookingOpt.get());
            List<PaymentResponseDto> responseList = payments.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
            return ApiResponse.success(responseList);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    private PaymentResponseDto mapToResponseDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        if (payment.getBooking() != null) {
            dto.setBookingId(payment.getBooking().getId());
        }
        dto.setAmount(payment.getAmount());
        dto.setPaymentType(payment.getPaymentType() != null ? payment.getPaymentType().name() : "ADVANCE");
        dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "OFFLINE_UPI");
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : "PENDING");
        dto.setTransactionId(payment.getTransactionId());
        dto.setScreenshot(payment.getScreenshot());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}