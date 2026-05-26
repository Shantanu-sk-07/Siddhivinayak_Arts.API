package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.service.BookingService;
import com.suraj.MurtiSystem.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/todays-pickups")
    public ApiResponse<List<Booking>> getTodaysPickups() {
        return staffService.getTodaysPickups();
    }

    @PostMapping("/complete-pickup/{bookingId}")
    public ApiResponse<Booking> completePickup(@PathVariable String bookingId) {
        return staffService.completePickup(bookingId);
    }

    @PostMapping("/verify-booking")
    public ApiResponse<Booking> verifyBooking(@RequestBody String bookingId) {
        return bookingService.updateBookingStatus(bookingId, "CONFIRMED");
    }
}