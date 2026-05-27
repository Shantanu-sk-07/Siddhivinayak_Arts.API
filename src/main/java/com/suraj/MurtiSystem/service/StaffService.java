package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.request.StaffRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.BookingResponseDto;
import com.suraj.MurtiSystem.dto.response.StaffResponseDto;
import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.entity.Staff;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.entity.User.UserRole;
import com.suraj.MurtiSystem.repository.BookingRepository;
import com.suraj.MurtiSystem.repository.StaffRepository;
import com.suraj.MurtiSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WhatsAppService whatsAppService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isSuperAdmin(User user) {
        return user.getRole() == UserRole.SUPER_ADMIN;
    }

    public ApiResponse<List<StaffResponseDto>> getAllStaff() {
        User currentUser = getCurrentUser();
        if (!isSuperAdmin(currentUser)) {
            return ApiResponse.error("Access denied. Only SUPER_ADMIN can view staff.");
        }
        List<Staff> staffList = staffRepository.findAll();
        List<StaffResponseDto> responseList = staffList.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ApiResponse.success(responseList);
    }

    public ApiResponse<List<User>> getAllCustomers() {
        User currentUser = getCurrentUser();
        if (!isSuperAdmin(currentUser)) {
            return ApiResponse.error("Access denied. Only SUPER_ADMIN can view customers.");
        }
        List<User> customers = userRepository.findAll();
        return ApiResponse.success(customers);
    }

    public ApiResponse<StaffResponseDto> addStaff(StaffRequestDto request) {
        User currentUser = getCurrentUser();
        if (!isSuperAdmin(currentUser)) {
            return ApiResponse.error("Access denied. Only SUPER_ADMIN can add staff.");
        }
        if (staffRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email already exists");
        }
        Staff staff = new Staff();
        staff.setName(request.getName());
        staff.setEmail(request.getEmail());
        staff.setPhone(request.getPhone());
        staff.setAssignedCounter(request.getAssignedCounter());
        staff.setIsActive(request.getIsActive());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setCreatedAt(LocalDateTime.now());
        Staff saved = staffRepository.save(staff);
        if (!userRepository.existsByEmail(request.getEmail())) {
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.STAFF);
            user.setIsActive(true);
            userRepository.save(user);
        }
        return ApiResponse.success(mapToResponseDto(saved), "Staff added successfully");
    }

    public ApiResponse<StaffResponseDto> updateStaff(String id, StaffRequestDto request) {
        User currentUser = getCurrentUser();
        if (!isSuperAdmin(currentUser)) {
            return ApiResponse.error("Access denied. Only SUPER_ADMIN can update staff.");
        }
        Optional<Staff> existingOpt = staffRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ApiResponse.error("Staff not found");
        }
        Staff existing = existingOpt.get();
        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setAssignedCounter(request.getAssignedCounter());
        existing.setIsActive(request.getIsActive());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            }
        }
        Staff updated = staffRepository.save(existing);
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(request.getName());
            user.setPhone(request.getPhone());
            user.setIsActive(request.getIsActive());
            userRepository.save(user);
        }
        return ApiResponse.success(mapToResponseDto(updated), "Staff updated successfully");
    }

    public ApiResponse<Void> deleteStaff(String id) {
        User currentUser = getCurrentUser();
        if (!isSuperAdmin(currentUser)) {
            return ApiResponse.error("Access denied. Only SUPER_ADMIN can delete staff.");
        }
        Optional<Staff> staffOpt = staffRepository.findById(id);
        if (staffOpt.isEmpty()) {
            return ApiResponse.error("Staff not found");
        }
        Staff staff = staffOpt.get();
        String email = staff.getEmail();
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(userRepository::delete);
        staffRepository.deleteById(id);
        return ApiResponse.success(null, "Staff removed successfully");
    }

    public ApiResponse<List<BookingResponseDto>> getTodaysPickups() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<Booking> pickups = bookingRepository.findByStatusAndBookingDateBetween(
                Booking.BookingStatus.CONFIRMED, startOfDay, endOfDay);
        List<BookingResponseDto> responseList = pickups.stream()
                .map(this::mapBookingToResponseDto)
                .collect(Collectors.toList());
        return ApiResponse.success(responseList);
    }

    public ApiResponse<BookingResponseDto> completePickup(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }
        Booking booking = bookingOpt.get();
        if (booking.getRemainingAmount() > 0) {
            return ApiResponse.error("Please complete pending payment first");
        }
        booking.setStatus(Booking.BookingStatus.PICKUP_COMPLETED);
        booking.setPickupDate(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        String message = whatsAppService.getPickupCompletedMessage(
                booking.getCustomer().getName(),
                booking.getGanpati().getName(),
                booking.getBookingId()
        );
        String whatsappLink = whatsAppService.generateWhatsAppLink(message);
        System.out.println("📱 WhatsApp Admin Link (Pickup): " + whatsappLink);
        return ApiResponse.success(mapBookingToResponseDto(saved), "Pickup completed successfully");
    }

    public ApiResponse<Map<String, Object>> getPickupStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<Booking> todayPickups = bookingRepository.findByStatusAndBookingDateBetween(
                Booking.BookingStatus.CONFIRMED, startOfDay, endOfDay);
        long completedToday = bookingRepository.findByStatusAndBookingDateBetween(
                Booking.BookingStatus.PICKUP_COMPLETED, startOfDay, endOfDay).size();
        stats.put("todayPickups", (long) todayPickups.size());
        stats.put("completedToday", completedToday);
        stats.put("pendingToday", todayPickups.size() - completedToday);
        stats.put("totalPickups", bookingRepository.findByStatus(Booking.BookingStatus.PICKUP_COMPLETED).size());
        return ApiResponse.success(stats);
    }

    public ApiResponse<Map<String, Object>> searchPickupByPhone(String phone) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("No user found with this phone number");
        }
        List<Booking> bookings = bookingRepository.findByCustomer(userOpt.get());
        Optional<Booking> pendingPickup = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED && b.getRemainingAmount() == 0)
                .findFirst();
        if (pendingPickup.isEmpty()) {
            return ApiResponse.error("No pending pickup found for this number");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("booking", mapBookingToResponseDto(pendingPickup.get()));
        return ApiResponse.success(result);
    }

    public ApiResponse<Map<String, Object>> searchBookingByPhone(String phone) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("No user found with this phone number");
        }
        List<Booking> bookings = bookingRepository.findByCustomer(userOpt.get());
        if (bookings.isEmpty()) {
            return ApiResponse.error("No bookings found for this number");
        }
        Booking latestBooking = bookings.stream()
                .max((b1, b2) -> b1.getCreatedAt().compareTo(b2.getCreatedAt()))
                .orElse(null);
        if (latestBooking == null) {
            return ApiResponse.error("No bookings found");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("booking", mapBookingToResponseDto(latestBooking));
        return ApiResponse.success(result);
    }

    public ApiResponse<Map<String, Object>> verifyPayment(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }
        Booking booking = bookingOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Payment verified successfully");
        result.put("booking", mapBookingToResponseDto(booking));
        return ApiResponse.success(result);
    }

    private StaffResponseDto mapToResponseDto(Staff staff) {
        StaffResponseDto dto = new StaffResponseDto();
        dto.setId(staff.getId());
        dto.setName(staff.getName());
        dto.setEmail(staff.getEmail());
        dto.setPhone(staff.getPhone());
        dto.setAssignedCounter(staff.getAssignedCounter());
        dto.setIsActive(staff.getIsActive());
        dto.setCreatedAt(staff.getCreatedAt());
        return dto;
    }

    private BookingResponseDto mapBookingToResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setBookingId(booking.getBookingId());
        if (booking.getGanpati() != null) {
            dto.setGanpatiId(booking.getGanpati().getId());
            dto.setGanpatiName(booking.getGanpati().getName());
        }
        if (booking.getCustomer() != null) {
            dto.setCustomerId(booking.getCustomer().getId());
            dto.setCustomerName(booking.getCustomer().getName());
            dto.setCustomerPhone(booking.getCustomer().getPhone());
        }
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setAdvancePaid(booking.getAdvancePaid());
        dto.setRemainingAmount(booking.getRemainingAmount());
        dto.setStatus(booking.getStatus().name());
        dto.setQrCode(booking.getQrCode());
        dto.setBookingDate(booking.getBookingDate());
        dto.setPickupDate(booking.getPickupDate());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }
}