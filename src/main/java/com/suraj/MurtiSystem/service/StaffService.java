package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.entity.Staff;
import com.suraj.MurtiSystem.repository.BookingRepository;
import com.suraj.MurtiSystem.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ApiResponse<List<Staff>> getAllStaff() {
        List<Staff> staffList = staffRepository.findAll();
        staffList.forEach(staff -> staff.setPassword(null));
        return ApiResponse.success(staffList);
    }

    public ApiResponse<Staff> addStaff(Staff staff) {
        if (staffRepository.existsByEmail(staff.getEmail())) {
            return ApiResponse.error("Email already exists");
        }
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        Staff saved = staffRepository.save(staff);
        saved.setPassword(null);
        return ApiResponse.success(saved, "Staff added successfully");
    }

    public ApiResponse<Staff> updateStaff(String id, Staff staffDetails) {
        Optional<Staff> existingOpt = staffRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ApiResponse.error("Staff not found");
        }

        Staff existing = existingOpt.get();
        existing.setName(staffDetails.getName());
        existing.setEmail(staffDetails.getEmail());
        existing.setPhone(staffDetails.getPhone());
        existing.setAssignedCounter(staffDetails.getAssignedCounter());
        existing.setIsActive(staffDetails.getIsActive());

        if (staffDetails.getPassword() != null && !staffDetails.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(staffDetails.getPassword()));
        }

        Staff updated = staffRepository.save(existing);
        updated.setPassword(null);
        return ApiResponse.success(updated, "Staff updated successfully");
    }

    public ApiResponse<Void> deleteStaff(String id) {
        if (!staffRepository.existsById(id)) {
            return ApiResponse.error("Staff not found");
        }
        staffRepository.deleteById(id);
        return ApiResponse.success(null, "Staff removed successfully");
    }

    public ApiResponse<List<Booking>> getTodaysPickups() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        List<Booking> pickups = bookingRepository.findByStatusAndBookingDateBetween(
                Booking.BookingStatus.CONFIRMED, start, end);

        return ApiResponse.success(pickups);
    }

    public ApiResponse<Booking> completePickup(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }

        Booking booking = bookingOpt.get();
        if (booking.getRemainingAmount() > 0) {
            return ApiResponse.error("Pending payment exists");
        }

        booking.setStatus(Booking.BookingStatus.PICKUP_COMPLETED);
        booking.setPickupDate(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        return ApiResponse.success(saved, "Pickup completed");
    }
}