package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.Interest;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.entity.Ganpati;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, String> {
    Optional<Interest> findByUserAndGanpati(User user, Ganpati ganpati);
    List<Interest> findByUser(User user);
    boolean existsByUserAndGanpati(User user, Ganpati ganpati);
    void deleteByUserAndGanpati(User user, Ganpati ganpati);
}