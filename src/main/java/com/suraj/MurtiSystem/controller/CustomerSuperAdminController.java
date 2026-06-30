package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.request.CustomerRegisterRequest;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.CustomerRegisterResponse;
import com.suraj.MurtiSystem.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
public class CustomerSuperAdminController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerSuperAdminController.class);

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ApiResponse<List<CustomerRegisterResponse>> getAllCustomers() {
        logger.info("=== GET ALL CUSTOMERS API ===");
        try {
            ApiResponse<List<CustomerRegisterResponse>> response = customerService.getAllCustomers();
            logger.info("Response success: {}, Data size: {}",
                    response.isSuccess(),
                    response.getData() != null ? response.getData().size() : 0);
            return response;
        } catch (Exception e) {
            logger.error("Error in getAllCustomers: ", e);
            return ApiResponse.error("Failed to fetch customers: " + e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<CustomerRegisterResponse>> getCustomersByType(@PathVariable String type) {
        logger.info("=== GET CUSTOMERS BY TYPE: {} ===", type);
        return customerService.getCustomersByType(type);
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerRegisterResponse> getCustomerById(@PathVariable String id) {
        logger.info("=== GET CUSTOMER BY ID: {} ===", id);
        return customerService.getCustomerById(id);
    }

    @PostMapping
    public ApiResponse<CustomerRegisterResponse> createCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        logger.info("=== ADMIN CREATE CUSTOMER API ===");
        logger.info("Name: {}, Phone: {}, Type: {}", request.getName(), request.getPhone(), request.getRegistrationType());
        return customerService.createCustomer(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerRegisterResponse> updateCustomer(@PathVariable String id, @Valid @RequestBody CustomerRegisterRequest request) {
        logger.info("=== ADMIN UPDATE CUSTOMER: {} ===", id);
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable String id) {
        logger.info("=== ADMIN DELETE CUSTOMER: {} ===", id);
        return customerService.deleteCustomer(id);
    }

    @PostMapping("/{id}/promote")
    public ApiResponse<CustomerRegisterResponse> promoteCustomer(@PathVariable String id) {
        logger.info("=== ADMIN PROMOTE CUSTOMER: {} ===", id);
        return customerService.promoteCustomer(id);
    }

    @PostMapping("/{id}/unpromote")
    public ApiResponse<CustomerRegisterResponse> unpromoteCustomer(@PathVariable String id) {
        logger.info("=== ADMIN UNPROMOTE CUSTOMER: {} ===", id);
        return customerService.unpromoteCustomer(id);
    }
}