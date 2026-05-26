package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.entity.Ganpati;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.repository.BookingRepository;
import com.suraj.MurtiSystem.repository.GanpatiRepository;
import com.suraj.MurtiSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GanpatiRepository ganpatiRepository;

    @Autowired
    private UserRepository userRepository;

    public ApiResponse<List<Booking>> getCustomerBookings(String customerId) {
        Optional<User> customer = userRepository.findById(customerId);
        if (customer.isEmpty()) {
            return ApiResponse.error("Customer not found");
        }
        List<Booking> bookings = bookingRepository.findByCustomer(customer.get());
        return ApiResponse.success(bookings);
    }

    public ApiResponse<Booking> requestBooking(String customerId, String ganpatiId) {
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

        boolean alreadyBooked = bookingRepository.existsByCustomerAndGanpatiAndStatusNot(
                customerOpt.get(), ganpati, Booking.BookingStatus.REJECTED);

        if (alreadyBooked) {
            return ApiResponse.error("You already have a booking request for this Ganpati");
        }

        Booking booking = new Booking();
        booking.setGanpati(ganpati);
        booking.setCustomer(customerOpt.get());
        booking.setTotalAmount(ganpati.getPrice());
        booking.setAdvancePaid(0.0);
        booking.setRemainingAmount(ganpati.getPrice());
        booking.setStatus(Booking.BookingStatus.PENDING_REQUEST);
        booking.setBookingDate(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        ganpati.setAvailableSlots(ganpati.getAvailableSlots() - 1);
        ganpatiRepository.save(ganpati);

        return ApiResponse.success(saved, "Booking request submitted successfully");
    }

    public ApiResponse<Booking> approveBooking(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus(Booking.BookingStatus.APPROVED);
        Booking saved = bookingRepository.save(booking);

        return ApiResponse.success(saved, "Booking approved successfully");
    }

    public ApiResponse<Booking> updateBookingStatus(String bookingId, String status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.error("Booking not found");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus(Booking.BookingStatus.valueOf(status));
        Booking saved = bookingRepository.save(booking);

        return ApiResponse.success(saved, "Booking status updated");
    }

    public ApiResponse<Booking> completePickup(String bookingId) {
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

        return ApiResponse.success(saved, "Pickup completed successfully");
    }
}