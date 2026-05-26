package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.entity.Payment;
import com.suraj.MurtiSystem.service.BookingService;
import com.suraj.MurtiSystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/bookings")
    public ApiResponse<List<Booking>> getMyBookings(@RequestHeader("Authorization") String token) {
        return bookingService.getCustomerBookings(getUserIdFromToken(token));
    }

    @PostMapping("/booking-request")
    public ApiResponse<Booking> requestBooking(@RequestHeader("Authorization") String token, @RequestBody String ganpatiId) {
        return bookingService.requestBooking(getUserIdFromToken(token), ganpatiId);
    }

    @GetMapping("/payments/{bookingId}")
    public ApiResponse<List<Payment>> getPayments(@PathVariable String bookingId) {
        return paymentService.getBookingPayments(bookingId);
    }

    @PostMapping("/payments/offline")
    public ApiResponse<Payment> submitOfflinePayment(
            @RequestParam String bookingId,
            @RequestParam Double amount,
            @RequestParam String transactionId,
            @RequestParam MultipartFile screenshot) {
        return paymentService.submitOfflinePayment(bookingId, amount, transactionId, screenshot);
    }

    private String getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}