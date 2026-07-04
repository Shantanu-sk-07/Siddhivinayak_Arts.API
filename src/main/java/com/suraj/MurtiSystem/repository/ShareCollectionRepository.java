package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.ShareCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShareCollectionRepository extends JpaRepository<ShareCollection, String> {
    Optional<ShareCollection> findByToken(String token);

    @Query("SELECT sc FROM ShareCollection sc WHERE sc.token = :token AND sc.isActive = true AND (sc.expiryDate IS NULL OR sc.expiryDate > CURRENT_TIMESTAMP)")
    Optional<ShareCollection> findValidByToken(@Param("token") String token);
}