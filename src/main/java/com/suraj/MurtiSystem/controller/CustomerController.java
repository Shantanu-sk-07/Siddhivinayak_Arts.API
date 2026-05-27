package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.request.CustomerUpdateRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.BookingResponseDto;
import com.suraj.MurtiSystem.dto.response.CustomerResponseDto;
import com.suraj.MurtiSystem.dto.response.PaymentResponseDto;
import com.suraj.MurtiSystem.entity.Ganpati;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.repository.GanpatiRepository;
import com.suraj.MurtiSystem.repository.UserRepository;
import com.suraj.MurtiSystem.service.BookingService;
import com.suraj.MurtiSystem.service.PaymentService;
import com.suraj.MurtiSystem.service.UserService;
import com.suraj.MurtiSystem.service.WhatsAppService;
import com.suraj.MurtiSystem.config.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GanpatiRepository ganpatiRepository;

    @GetMapping("/profile")
    public ApiResponse<CustomerResponseDto> getProfile(@RequestHeader(value = "Authorization", required = false) String token) {
        String userId = getUserIdFromToken(token);
        return userService.getCustomerById(userId);
    }

    @PutMapping("/profile")
    public ApiResponse<CustomerResponseDto> updateProfile(@RequestHeader(value = "Authorization", required = false) String token,
                                                          @Valid @RequestBody CustomerUpdateRequestDto request) {
        String userId = getUserIdFromToken(token);
        return userService.updateCustomer(userId, request);
    }

    @GetMapping("/bookings")
    public ApiResponse<List<BookingResponseDto>> getMyBookings(@RequestHeader(value = "Authorization", required = false) String token) {
        String userId = getUserIdFromToken(token);
        return bookingService.getCustomerBookings(userId);
    }

    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<BookingResponseDto> getBookingById(@PathVariable String bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    @PostMapping("/booking-request")
    public ApiResponse<BookingResponseDto> requestBooking(@RequestHeader(value = "Authorization", required = false) String token,
                                                          @Valid @RequestBody Map<String, String> body) {
        String userId = getUserIdFromToken(token);
        String ganpatiId = body.get("ganpatiId");
        System.out.println("DEBUG: userId from token = " + userId);
        System.out.println("DEBUG: ganpatiId = " + ganpatiId);
        return bookingService.requestBooking(userId, ganpatiId);
    }

    @GetMapping("/qr/{bookingId}")
    public ApiResponse<Map<String, Object>> getQRCodeData(@PathVariable String bookingId) {
        return bookingService.getQRCodeData(bookingId);
    }

    @GetMapping("/payments/{bookingId}")
    public ApiResponse<List<PaymentResponseDto>> getPayments(@PathVariable String bookingId) {
        return paymentService.getBookingPayments(bookingId);
    }

    @GetMapping("/payments/all")
    public ApiResponse<List<PaymentResponseDto>> getAllPayments(@RequestHeader(value = "Authorization", required = false) String token) {
        String userId = getUserIdFromToken(token);
        return paymentService.getCustomerPayments(userId);
    }

    @PostMapping("/payments/offline")
    public ApiResponse<PaymentResponseDto> submitOfflinePayment(
            @RequestParam String bookingId,
            @RequestParam Double amount,
            @RequestParam String transactionId,
            @RequestParam(required = false) MultipartFile screenshot) {
        return paymentService.submitOfflinePayment(bookingId, amount, transactionId, screenshot);
    }

    @PostMapping("/receipt/{bookingId}")
    public ResponseEntity<ByteArrayResource> downloadReceipt(@PathVariable String bookingId) {
        String receiptHtml = "<html><body>" +
                "<h1>Siddhivinayak Arts</h1>" +
                "<h3>Booking Receipt</h3>" +
                "<p>Booking ID: " + bookingId + "</p>" +
                "<p>Thank you for your booking!</p>" +
                "</body></html>";

        byte[] receiptBytes = receiptHtml.getBytes();
        ByteArrayResource resource = new ByteArrayResource(receiptBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt_" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/interested")
    public ApiResponse<List<String>> getInterestedItems() {
        return ApiResponse.success(new ArrayList<>(), "No interested items");
    }

    @GetMapping("/interested/check/{ganpatiId}")
    public ApiResponse<Map<String, Boolean>> checkInterested(@PathVariable String ganpatiId) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("isInterested", false);
        return ApiResponse.success(result);
    }

    @PostMapping("/interested/toggle")
    public ApiResponse<Void> toggleInterested(@RequestBody Map<String, String> body,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        String userId = getUserIdFromToken(token);
        String ganpatiId = body.get("ganpatiId");

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Ganpati> ganpatiOpt = ganpatiRepository.findById(ganpatiId);

        if (userOpt.isPresent() && ganpatiOpt.isPresent()) {
            String message = whatsAppService.getInterestedMessage(
                    userOpt.get().getName(),
                    userOpt.get().getPhone(),
                    ganpatiOpt.get().getName(),
                    ganpatiOpt.get().getPrice()
            );
            String whatsappLink = whatsAppService.generateWhatsAppLink(message);
            System.out.println("📱 WhatsApp Admin Link (Interest): " + whatsappLink);
        }

        return ApiResponse.success(null, "Toggled");
    }

    @GetMapping("/booking/check/{ganpatiId}")
    public ApiResponse<Map<String, Object>> checkExistingBooking(@PathVariable String ganpatiId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("booking", null);
        return ApiResponse.success(result);
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> getCustomerSummary(@RequestHeader(value = "Authorization", required = false) String token) {
        String userId = getUserIdFromToken(token);
        Map<String, Object> summary = userService.getCustomerSummary(userId);
        return ApiResponse.success(summary);
    }

    private String getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return "test-user-id";
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId != null && !userId.isEmpty()) {
                return userId;
            }
        } catch (Exception e) {
            System.err.println("Error extracting userId from token: " + e.getMessage());
        }
        return "test-user-id";
    }
}