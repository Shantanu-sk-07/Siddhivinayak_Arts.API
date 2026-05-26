package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.entity.Ganpati;
import com.suraj.MurtiSystem.entity.Payment;
import com.suraj.MurtiSystem.entity.Staff;
import com.suraj.MurtiSystem.repository.BookingRepository;
import com.suraj.MurtiSystem.service.BookingService;
import com.suraj.MurtiSystem.service.GanpatiService;
import com.suraj.MurtiSystem.service.PaymentService;
import com.suraj.MurtiSystem.service.StaffService;
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

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping("/ganpati")
    public ApiResponse<Ganpati> createGanpati(
            @RequestPart("ganpati") Ganpati ganpati,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ganpatiService.createGanpati(ganpati, images);
    }

    @PutMapping("/ganpati/{id}")
    public ApiResponse<Ganpati> updateGanpati(
            @PathVariable String id,
            @RequestPart("ganpati") Ganpati ganpati,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ganpatiService.updateGanpati(id, ganpati, images);
    }

    @DeleteMapping("/ganpati/{id}")
    public ApiResponse<Void> deleteGanpati(@PathVariable String id) {
        return ganpatiService.deleteGanpati(id);
    }

    @GetMapping("/bookings")
    public ApiResponse<List<Booking>> getAllBookings() {
        return ApiResponse.success(bookingRepository.findAll());
    }

    @PostMapping("/bookings/{id}/approve")
    public ApiResponse<Booking> approveBooking(@PathVariable String id) {
        return bookingService.approveBooking(id);
    }

    @PostMapping("/bookings/{id}/reject")
    public ApiResponse<Booking> rejectBooking(@PathVariable String id) {
        return bookingService.updateBookingStatus(id, "REJECTED");
    }

    @PutMapping("/bookings/{id}/status")
    public ApiResponse<Booking> updateBookingStatus(@PathVariable String id, @RequestBody String status) {
        return bookingService.updateBookingStatus(id, status);
    }

    @GetMapping("/payments/pending")
    public ApiResponse<List<Payment>> getPendingPayments() {
        return paymentService.getPendingPayments();
    }

    @PostMapping("/payments/{id}/verify")
    public ApiResponse<Payment> verifyPayment(@PathVariable String id, @RequestBody String status) {
        return paymentService.verifyPayment(id, status, "admin_id");
    }

    @GetMapping("/staff")
    public ApiResponse<List<Staff>> getAllStaff() {
        return staffService.getAllStaff();
    }

    @PostMapping("/staff")
    public ApiResponse<Staff> addStaff(@RequestBody Staff staff) {
        return staffService.addStaff(staff);
    }

    @PutMapping("/staff/{id}")
    public ApiResponse<Staff> updateStaff(@PathVariable String id, @RequestBody Staff staff) {
        return staffService.updateStaff(id, staff);
    }

    @DeleteMapping("/staff/{id}")
    public ApiResponse<Void> deleteStaff(@PathVariable String id) {
        return staffService.deleteStaff(id);
    }
}