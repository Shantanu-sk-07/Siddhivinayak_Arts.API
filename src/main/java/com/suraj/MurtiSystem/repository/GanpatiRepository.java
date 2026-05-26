package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.Ganpati;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GanpatiRepository extends JpaRepository<Ganpati, String> {
    List<Ganpati> findByIsActiveTrue();

    @Query("SELECT g FROM Ganpati g WHERE g.isActive = true ORDER BY g.rating DESC")
    List<Ganpati> findFeaturedGanpati();

    @Query("SELECT g FROM Ganpati g WHERE g.isActive = true AND g.availableSlots > 0")
    List<Ganpati> findAvailableGanpati();

    @Query("SELECT g FROM Ganpati g WHERE g.name LIKE %:keyword% AND g.isActive = true")
    List<Ganpati> searchByName(@Param("keyword") String keyword);
}