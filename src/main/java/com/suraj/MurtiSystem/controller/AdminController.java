package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.request.GanpatiRequestDto;
import com.suraj.MurtiSystem.dto.request.StaffRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.BookingResponseDto;
import com.suraj.MurtiSystem.dto.response.GanpatiResponseDto;
import com.suraj.MurtiSystem.dto.response.PaymentResponseDto;
import com.suraj.MurtiSystem.dto.response.StaffResponseDto;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.service.BookingService;
import com.suraj.MurtiSystem.service.GanpatiService;
import com.suraj.MurtiSystem.service.PaymentService;
import com.suraj.MurtiSystem.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private GanpatiService ganpatiService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private StaffService staffService;

    @GetMapping("/ganpati")
    public ApiResponse<List<GanpatiResponseDto>> getAllGanpati() {
        return ganpatiService.getAllGanpati();
    }

    @PostMapping(value = "/ganpati", consumes = "multipart/form-data")
    public ApiResponse<GanpatiResponseDto> createGanpati(@Valid @ModelAttribute GanpatiRequestDto request) {
        return ganpatiService.createGanpati(request);
    }

    @PutMapping(value = "/ganpati/{id}", consumes = "multipart/form-data")
    public ApiResponse<GanpatiResponseDto> updateGanpati(@PathVariable String id, @Valid @ModelAttribute GanpatiRequestDto request) {
        return ganpatiService.updateGanpati(id, request);
    }

    @DeleteMapping("/ganpati/{id}")
    public ApiResponse<Void> deleteGanpati(@PathVariable String id) {
        return ganpatiService.deleteGanpati(id);
    }

    @GetMapping("/bookings")
    public ApiResponse<List<BookingResponseDto>> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping("/bookings/{id}/approve")
    public ApiResponse<BookingResponseDto> approveBooking(@PathVariable String id) {
        return bookingService.approveBooking(id);
    }

    @PostMapping("/bookings/{id}/reject")
    public ApiResponse<BookingResponseDto> rejectBooking(@PathVariable String id) {
        return bookingService.rejectBooking(id);
    }

    @PutMapping("/bookings/{id}/status")
    public ApiResponse<BookingResponseDto> updateBookingStatus(@PathVariable String id, @RequestBody String status) {
        return bookingService.updateBookingStatus(id, status);
    }

    @GetMapping("/payments/pending")
    public ApiResponse<List<PaymentResponseDto>> getPendingPayments() {
        return paymentService.getPendingPayments();
    }

    @PostMapping("/payments/{id}/verify")
    public ApiResponse<PaymentResponseDto> verifyPayment(@PathVariable String id, @RequestBody String status) {
        return paymentService.verifyPayment(id, status, "admin_id");
    }

    @GetMapping("/staff")
    public ApiResponse<List<StaffResponseDto>> getAllStaff() {
        return staffService.getAllStaff();
    }

    @PostMapping("/staff")
    public ApiResponse<StaffResponseDto> addStaff(@Valid @RequestBody StaffRequestDto request) {
        return staffService.addStaff(request);
    }

    @PutMapping("/staff/{id}")
    public ApiResponse<StaffResponseDto> updateStaff(@PathVariable String id, @Valid @RequestBody StaffRequestDto request) {
        return staffService.updateStaff(id, request);
    }

    @DeleteMapping("/staff/{id}")
    public ApiResponse<Void> deleteStaff(@PathVariable String id) {
        return staffService.deleteStaff(id);
    }

    @GetMapping("/customers")
    public ApiResponse<List<User>> getAllCustomers() {
        return staffService.getAllCustomers();
    }
}