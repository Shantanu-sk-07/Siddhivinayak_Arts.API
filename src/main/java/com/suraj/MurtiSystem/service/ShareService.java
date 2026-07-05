package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.request.ShareCollectionRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.ShareCollectionResponseDto;
import com.suraj.MurtiSystem.entity.*;
import com.suraj.MurtiSystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShareService {

    @Autowired
    private ShareCollectionRepository shareCollectionRepository;

    @Autowired
    private ShareCollectionGanpatiRepository shareCollectionGanpatiRepository;

    @Autowired
    private ShareCollectionCustomerRepository shareCollectionCustomerRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private GanpatiRepository ganpatiRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ApiResponse<ShareCollectionResponseDto> createShareCollection(ShareCollectionRequestDto request, String adminId) {
        try {
            Owner admin = ownerRepository.findById(adminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            ShareCollection shareCollection = new ShareCollection();
            shareCollection.setToken(generateSecureToken());
            shareCollection.setCreatedBy(admin);
            shareCollection.setIsActive(true);

            ShareCollection saved = shareCollectionRepository.save(shareCollection);

            if (request.getGanpatiIds() != null) {
                for (String ganpatiId : request.getGanpatiIds()) {
                    Ganpati ganpati = ganpatiRepository.findById(ganpatiId)
                            .orElseThrow(() -> new RuntimeException("Ganpati not found: " + ganpatiId));
                    ShareCollectionGanpati collectionGanpati = new ShareCollectionGanpati();
                    collectionGanpati.setShareCollection(saved);
                    collectionGanpati.setGanpati(ganpati);
                    shareCollectionGanpatiRepository.save(collectionGanpati);
                }
            }

            if (request.getCustomerIds() != null) {
                for (String customerId : request.getCustomerIds()) {
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
                    ShareCollectionCustomer collectionCustomer = new ShareCollectionCustomer();
                    collectionCustomer.setShareCollection(saved);
                    collectionCustomer.setCustomer(customer);
                    shareCollectionCustomerRepository.save(collectionCustomer);
                }
            }

            ShareCollectionResponseDto response = mapToResponse(saved);
            response.setShareUrl(baseUrl + "/view/" + saved.getToken());

            return ApiResponse.success(response, "Share collection created successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to create share collection: " + e.getMessage());
        }
    }

    public ApiResponse<ShareCollectionResponseDto> getShareCollection(String token) {
        try {
            ShareCollection shareCollection = shareCollectionRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid link"));

            if (!shareCollection.getIsActive()) {
                return ApiResponse.error("This link has been deactivated.");
            }

            ShareCollectionResponseDto response = mapToResponse(shareCollection);
            response.setShareUrl(baseUrl + "/view/" + token);

            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("Invalid or deactivated link");
        }
    }

    public ApiResponse<Void> deactivateShareCollection(String token) {
        try {
            ShareCollection shareCollection = shareCollectionRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Share collection not found"));
            shareCollection.setIsActive(false);
            shareCollectionRepository.save(shareCollection);
            return ApiResponse.success(null, "Share collection deactivated");
        } catch (Exception e) {
            return ApiResponse.error("Failed to deactivate: " + e.getMessage());
        }
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                System.currentTimeMillis() +
                UUID.randomUUID().toString().substring(0, 8);
    }

    private ShareCollectionResponseDto mapToResponse(ShareCollection shareCollection) {
        ShareCollectionResponseDto dto = new ShareCollectionResponseDto();
        dto.setId(shareCollection.getId());
        dto.setToken(shareCollection.getToken());
        dto.setCreatedBy(shareCollection.getCreatedBy().getName());
        dto.setCreatedDate(shareCollection.getCreatedDate());
        dto.setExpiryDate(null);
        dto.setIsActive(shareCollection.getIsActive());

        List<String> ganpatiIds = shareCollection.getGanpatis().stream()
                .map(g -> g.getGanpati().getId())
                .toList();
        dto.setGanpatiIds(ganpatiIds);

        List<String> customerIds = shareCollection.getCustomers().stream()
                .map(c -> c.getCustomer().getId())
                .toList();
        dto.setCustomerIds(customerIds);

        return dto;
    }
}