package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, String> {
    Optional<Receipt> findByToken(String token);

    @Query("SELECT r FROM Receipt r WHERE r.token = :token AND r.isActive = true")
    Optional<Receipt> findValidByToken(@Param("token") String token);
    Optional<Receipt> findByBooking_Id(String bookingId);
}