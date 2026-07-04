package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.ShareCollectionCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareCollectionCustomerRepository extends JpaRepository<ShareCollectionCustomer, String> {
}