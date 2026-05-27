package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.request.CustomerRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.CustomerResponseDto;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private UserRepository userRepository;

    public ApiResponse<CustomerResponseDto> getCustomerById(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found");
        }
        return ApiResponse.success(mapToResponseDto(userOpt.get()));
    }

    public ApiResponse<CustomerResponseDto> updateCustomer(String userId, CustomerRequestDto request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOpt.get();
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        // Password is not updated here (should be handled separately for security)

        User saved = userRepository.save(user);
        return ApiResponse.success(mapToResponseDto(saved), "Profile updated successfully");
    }

    public Map<String, Object> getCustomerSummary(String userId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("activeBookings", 0);
        summary.put("completedBookings", 0);
        summary.put("totalPaid", 0);
        summary.put("pendingAmount", 0);
        return summary;
    }

    private CustomerResponseDto mapToResponseDto(User user) {
        CustomerResponseDto dto = new CustomerResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}