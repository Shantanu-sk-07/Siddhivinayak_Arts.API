package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {
    Optional<Staff> findByEmail(String email);
    Optional<Staff> findByPhone(String phone);
    boolean existsByEmail(String email);
}