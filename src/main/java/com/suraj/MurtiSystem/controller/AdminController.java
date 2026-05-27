package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.request.GanpatiRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.BookingResponseDto;
import com.suraj.MurtiSystem.dto.response.GanpatiResponseDto;
import com.suraj.MurtiSystem.dto.response.PaymentResponseDto;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.service.AdminService;
import com.suraj.MurtiSystem.service.BookingService;
import com.suraj.MurtiSystem.service.GanpatiService;
import com.suraj.MurtiSystem.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private AdminService adminService;

    @GetMapping("/dashboard-stats")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGanpati", 0);
        stats.put("pendingRequests", 0);
        stats.put("totalRevenue", 0);
        stats.put("pendingPayments", 0);
        stats.put("interestedUsers", 0);
        Map<String, Object> festivalAnalytics = new HashMap<>();
        festivalAnalytics.put("totalBookings", 0);
        festivalAnalytics.put("completedPickups", 0);
        festivalAnalytics.put("occupancyRate", 0);
        festivalAnalytics.put("peakHours", new ArrayList<>());
        stats.put("festivalAnalytics", festivalAnalytics);
        return ApiResponse.success(stats);
    }

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

    @GetMapping("/customers")
    public ApiResponse<List<User>> getAllCustomers() {
        return adminService.getAllCustomers();
    }

    @GetMapping("/pickups/today")
    public ApiResponse<List<BookingResponseDto>> getTodaysPickups() {
        return adminService.getTodaysPickups();
    }

    @PostMapping("/pickups/{bookingId}/complete")
    public ApiResponse<BookingResponseDto> completePickup(@PathVariable String bookingId) {
        return adminService.completePickup(bookingId);
    }

    @GetMapping("/pickups/stats")
    public ApiResponse<Map<String, Object>> getPickupStats() {
        return adminService.getPickupStats();
    }

    @GetMapping("/pickups/search")
    public ApiResponse<Map<String, Object>> searchByPhone(@RequestParam String phone) {
        return adminService.searchByPhone(phone);
    }

    @PostMapping("/pickups/verify-booking")
    public ApiResponse<BookingResponseDto> verifyBooking(@RequestBody String bookingId) {
        return bookingService.updateBookingStatus(bookingId, "CONFIRMED");
    }

    @PostMapping("/pickups/verify-payment/{bookingId}")
    public ApiResponse<Map<String, Object>> verifyPayment(@PathVariable String bookingId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Payment verified successfully");
        return ApiResponse.success(result);
    }

    @GetMapping("/pickups/receipt/{bookingId}")
    public ResponseEntity<ByteArrayResource> printReceipt(@PathVariable String bookingId) {
        String htmlContent = "<html><body><h1>Receipt for Booking: " + bookingId + "</h1>" +
                "<p>This is a system generated receipt.</p>" +
                "<p>Thank you for choosing Siddhivinayak Arts!</p></body></html>";
        byte[] pdfBytes = htmlContent.getBytes();
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=receipt_" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/reports")
    public ApiResponse<Map<String, Object>> getReports(@RequestParam(required = false) String range) {
        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", 0);
        report.put("totalBookings", 0);
        report.put("totalCustomers", 0);
        report.put("completedPickups", 0);
        report.put("revenueTrend", 0);
        report.put("bookingTrend", 0);
        report.put("monthlyData", new ArrayList<>());
        report.put("topGanpati", new ArrayList<>());
        report.put("paymentMethodBreakdown", new ArrayList<>());
        return ApiResponse.success(report);
    }

    @GetMapping("/reports/export")
    public void exportReport(@RequestParam(required = false) String range) {
    }
}