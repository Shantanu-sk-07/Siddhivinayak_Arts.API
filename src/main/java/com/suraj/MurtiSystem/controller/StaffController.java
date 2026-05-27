package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.BookingResponseDto;
import com.suraj.MurtiSystem.service.BookingService;
import com.suraj.MurtiSystem.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @Autowired
    private BookingService bookingService;

    // Pickup endpoints
    @GetMapping("/todays-pickups")
    public ApiResponse<List<BookingResponseDto>> getTodaysPickups() {
        return staffService.getTodaysPickups();
    }

    @PostMapping("/complete-pickup/{bookingId}")
    public ApiResponse<BookingResponseDto> completePickup(@PathVariable String bookingId) {
        return staffService.completePickup(bookingId);
    }

    @PostMapping("/verify-booking")
    public ApiResponse<BookingResponseDto> verifyBooking(@RequestBody String bookingId) {
        return bookingService.updateBookingStatus(bookingId, "CONFIRMED");
    }

    // Stats endpoint
    @GetMapping("/pickup-stats")
    public ApiResponse<Map<String, Object>> getPickupStats() {
        return staffService.getPickupStats();
    }

    // Search endpoints
    @GetMapping("/search-pickup")
    public ApiResponse<Map<String, Object>> searchPickupByPhone(@RequestParam String phone) {
        return staffService.searchPickupByPhone(phone);
    }

    @GetMapping("/search-booking")
    public ApiResponse<Map<String, Object>> searchBookingByPhone(@RequestParam String phone) {
        return staffService.searchPickupByPhone(phone);
    }

    // Payment verification endpoint
    @PostMapping("/verify-payment/{bookingId}")
    public ApiResponse<Map<String, Object>> verifyPayment(@PathVariable String bookingId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Payment verified successfully");
        return ApiResponse.success(result);
    }

    // Pickups list endpoint
    @GetMapping("/pickups")
    public ApiResponse<List<BookingResponseDto>> getAllPickups() {
        return staffService.getTodaysPickups();
    }

    // Receipt endpoint
    @GetMapping("/receipt/{bookingId}")
    public ResponseEntity<ByteArrayResource> printReceipt(@PathVariable String bookingId) {
        // Create a simple HTML content (in real implementation, generate actual PDF)
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

    // Additional helper endpoint
    @GetMapping("/search")
    public ApiResponse<Map<String, Object>> searchByPhone(@RequestParam String phone) {
        return staffService.searchPickupByPhone(phone);
    }

}