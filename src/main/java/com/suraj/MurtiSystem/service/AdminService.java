package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.BookingResponseDto;
import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.entity.User.UserRole;
import com.suraj.MurtiSystem.repository.BookingRepository;
import com.suraj.MurtiSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

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

    public ApiResponse<List<User>> getAllCustomers() {
        User currentUser = getCurrentUser();
        if (!isSuperAdmin(currentUser)) {
            return ApiResponse.error("Access denied. Only SUPER_ADMIN can view customers.");
        }
        List<User> customers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.CUSTOMER)
                .collect(Collectors.toList());
        return ApiResponse.success(customers);
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
        whatsAppService.generateWhatsAppLink(message);
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

    public ApiResponse<Map<String, Object>> searchByPhone(String phone) {
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