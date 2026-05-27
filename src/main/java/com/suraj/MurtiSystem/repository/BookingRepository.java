package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.Booking;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.entity.Ganpati;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByCustomer(User customer);
    List<Booking> findByStatus(Booking.BookingStatus status);
    Optional<Booking> findByBookingId(String bookingId);
    void deleteByGanpatiId(String ganpatiId);

    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") String customerId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.bookingDate BETWEEN :start AND :end")
    List<Booking> findByStatusAndBookingDateBetween(@Param("status") Booking.BookingStatus status,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    boolean existsByCustomerAndGanpatiAndStatusNot(User customer, Ganpati ganpati, Booking.BookingStatus status);
}