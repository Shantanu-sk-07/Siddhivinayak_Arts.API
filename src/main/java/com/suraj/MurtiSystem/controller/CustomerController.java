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

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register")
    public ApiResponse<CustomerRegisterResponse> registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        logger.info("=== PUBLIC REGISTER CUSTOMER API ===");
        logger.info("Name: {}, Phone: {}, Type: {}", request.getName(), request.getPhone(), request.getRegistrationType());

        try {
            ApiResponse<CustomerRegisterResponse> response = customerService.registerCustomer(request);
            logger.info("Registration response - Success: {}, Message: {}", response.isSuccess(), response.getMessage());

            if (response.isSuccess() && response.getData() != null) {
                logger.info("Registered Customer ID: {}, Name: {}",
                        response.getData().getId(),
                        response.getData().getName());
            }
            return response;
        } catch (Exception e) {
            logger.error("Error in registerCustomer: ", e);
            return ApiResponse.error("Registration failed: " + e.getMessage());
        }
    }
}