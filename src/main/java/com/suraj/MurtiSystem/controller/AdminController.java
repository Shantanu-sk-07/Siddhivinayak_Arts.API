package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.request.GanpatiRequestDto;
import com.suraj.MurtiSystem.dto.request.ConfirmedBookingRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.GanpatiResponseDto;
import com.suraj.MurtiSystem.dto.response.ConfirmedBookingResponseDto;
import com.suraj.MurtiSystem.service.GanpatiService;
import com.suraj.MurtiSystem.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private GanpatiService ganpatiService;

    @Autowired
    private BookingService bookingService;

    // ========== Ganpati Management ==========
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

    // ========== Booking Management ==========
    @GetMapping("/bookings")
    public ApiResponse<List<ConfirmedBookingResponseDto>> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/bookings/{id}")
    public ApiResponse<ConfirmedBookingResponseDto> getBookingById(@PathVariable String id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping("/bookings")
    public ApiResponse<ConfirmedBookingResponseDto> createBooking(@Valid @RequestBody ConfirmedBookingRequestDto request) {
        return bookingService.createBooking(request);
    }

    @PutMapping("/bookings/{id}")
    public ApiResponse<ConfirmedBookingResponseDto> updateBooking(@PathVariable String id, @Valid @RequestBody ConfirmedBookingRequestDto request) {
        return bookingService.updateBooking(id, request);
    }

    @DeleteMapping("/bookings/{id}")
    public ApiResponse<Void> deleteBooking(@PathVariable String id) {
        return bookingService.deleteBooking(id);
    }

    @PostMapping("/bookings/{id}/send-receipt")
    public ApiResponse<String> sendReceiptToWhatsApp(@PathVariable String id) {
        return bookingService.sendReceiptToWhatsApp(id);
    }
}