package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.ConfirmedBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConfirmedBookingRepository extends JpaRepository<ConfirmedBooking, String> {
    List<ConfirmedBooking> findByCustomerId(String customerId);
    List<ConfirmedBooking> findByStatus(String status);

    @Query("SELECT b FROM ConfirmedBooking b ORDER BY b.createdAt DESC")
    List<ConfirmedBooking> findAllOrderByDateDesc();

    @Query("SELECT b FROM ConfirmedBooking b WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<ConfirmedBooking> findByStatusOrderByCreatedAt(@Param("status") String status);
}