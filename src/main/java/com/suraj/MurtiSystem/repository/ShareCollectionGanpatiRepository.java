package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.ShareCollectionGanpati;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareCollectionGanpatiRepository extends JpaRepository<ShareCollectionGanpati, String> {
}