package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.BookingResponseDto;
import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.entity.Ganpati;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.repository.BookingRepository;
import com.suraj.MurtiSystem.repository.GanpatiRepository;
import com.suraj.MurtiSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GanpatiRepository ganpatiRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WhatsAppService whatsAppService;

    public ApiResponse<List<BookingResponseDto>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        List<BookingResponseDto> responseList = bookings.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ApiResponse.success(responseList);
    }

    public ApiResponse<List<BookingResponseDto>> getCustomerBookings(String customerId) {
        Optional<User> customer = userRepository.findById(customerId);
        if (customer.isEmpty()) {
            return ApiResponse.success(List.of());
        }
        List<Booking> bookings = bookingRepository.findByCustomer(customer.get());
        List<BookingResponseDto> responseList = bookings.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ApiResponse.success(responseList);
    }

    public ApiResponse<BookingResponseDto> getBookingById(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }
        return ApiResponse.success(mapToResponseDto(bookingOpt.get()));
    }

    public ApiResponse<BookingResponseDto> requestBooking(String customerId, String ganpatiId) {
        Optional<User> customerOpt = userRepository.findById(customerId);
        Optional<Ganpati> ganpatiOpt = ganpatiRepository.findById(ganpatiId);

        if (customerOpt.isEmpty()) {
            return ApiResponse.error("Customer not found");
        }
        if (ganpatiOpt.isEmpty()) {
            return ApiResponse.error("Ganpati not found");
        }

        Ganpati ganpati = ganpatiOpt.get();

        if (ganpati.getAvailableSlots() <= 0) {
            return ApiResponse.error("No slots available");
        }

        Booking booking = new Booking();
        booking.setGanpati(ganpati);
        booking.setCustomer(customerOpt.get());
        booking.setTotalAmount(ganpati.getPrice());
        booking.setAdvancePaid(0.0);
        booking.setRemainingAmount(ganpati.getPrice());
        booking.setStatus(Booking.BookingStatus.PENDING_REQUEST);
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingId("BK" + System.currentTimeMillis());

        Booking saved = bookingRepository.save(booking);

        ganpati.setAvailableSlots(ganpati.getAvailableSlots() - 1);
        ganpatiRepository.save(ganpati);

        String message = whatsAppService.getBookingRequestMessage(
                customerOpt.get().getName(),
                customerOpt.get().getPhone(),
                ganpati.getName(),
                ganpati.getPrice(),
                saved.getBookingId()
        );
        String whatsappLink = whatsAppService.generateWhatsAppLink(message);
        System.out.println("📱 WhatsApp Admin Link: " + whatsappLink);

        return ApiResponse.success(mapToResponseDto(saved), "Booking request submitted successfully");
    }

    public ApiResponse<BookingResponseDto> approveBooking(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus(Booking.BookingStatus.APPROVED);
        Booking saved = bookingRepository.save(booking);

        String adminMessage = whatsAppService.getBookingApprovedMessage(
                booking.getCustomer().getName(),
                booking.getGanpati().getName(),
                saved.getBookingId(),
                saved.getTotalAmount() * 0.3,
                booking.getCustomer().getPhone()
        );
        String adminWhatsappLink = whatsAppService.generateWhatsAppLink(adminMessage);
        System.out.println("📱 WhatsApp Admin Link (Approved): " + adminWhatsappLink);

        return ApiResponse.success(mapToResponseDto(saved), "Booking approved successfully");
    }

    public ApiResponse<BookingResponseDto> rejectBooking(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }

        Booking booking = bookingOpt.get();

        Ganpati ganpati = booking.getGanpati();
        ganpati.setAvailableSlots(ganpati.getAvailableSlots() + 1);
        ganpatiRepository.save(ganpati);

        booking.setStatus(Booking.BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);

        return ApiResponse.success(mapToResponseDto(saved), "Booking rejected");
    }

    public ApiResponse<BookingResponseDto> updateBookingStatus(String bookingId, String status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus(Booking.BookingStatus.valueOf(status));
        Booking saved = bookingRepository.save(booking);

        return ApiResponse.success(mapToResponseDto(saved), "Booking status updated");
    }

    public ApiResponse<List<BookingResponseDto>> getTodaysPickups() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<Booking> pickups = bookingRepository.findByStatusAndBookingDateBetween(
                Booking.BookingStatus.CONFIRMED, startOfDay, endOfDay);

        List<BookingResponseDto> responseList = pickups.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ApiResponse.success(responseList);
    }

    public ApiResponse<Map<String, Object>> getQRCodeData(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }

        Booking booking = bookingOpt.get();

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED &&
                booking.getStatus() != Booking.BookingStatus.PICKUP_COMPLETED) {
            return ApiResponse.error("QR code is only available for confirmed bookings");
        }

        Map<String, Object> qrData = new HashMap<>();
        qrData.put("bookingId", booking.getBookingId());
        qrData.put("customerName", booking.getCustomer().getName());
        qrData.put("customerPhone", booking.getCustomer().getPhone());
        qrData.put("ganpatiName", booking.getGanpati().getName());
        qrData.put("totalAmount", booking.getTotalAmount());
        qrData.put("advancePaid", booking.getAdvancePaid());
        qrData.put("remainingAmount", booking.getRemainingAmount());
        qrData.put("status", booking.getStatus().name());
        qrData.put("bookingDate", booking.getBookingDate());
        qrData.put("timestamp", LocalDateTime.now().toString());

        return ApiResponse.success(qrData);
    }

    private BookingResponseDto mapToResponseDto(Booking booking) {
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