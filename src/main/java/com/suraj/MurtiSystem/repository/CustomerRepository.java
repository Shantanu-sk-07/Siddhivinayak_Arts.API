package com.suraj.MurtiSystem.repository;

import com.suraj.MurtiSystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByPhone(String phone);
    boolean existsByPhone(String phone);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.alternatePhone = :alternatePhone")
    boolean existsByAlternatePhone(@Param("alternatePhone") String alternatePhone);

    @Query("SELECT c FROM Customer c ORDER BY c.createdAt DESC")
    List<Customer> findAllCustomers();

    @Query("SELECT c FROM Customer c WHERE c.registrationType = :type")
    List<Customer> findCustomersByRegistrationType(@Param("type") Customer.RegistrationType type);

    @Query("SELECT COUNT(c) FROM Customer c")
    long countAllCustomers();
}