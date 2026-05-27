package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.request.GanpatiRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.GanpatiResponseDto;
import com.suraj.MurtiSystem.entity.Ganpati;
import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.repository.BookingRepository;
import com.suraj.MurtiSystem.repository.GanpatiRepository;
import com.suraj.MurtiSystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GanpatiService {

    @Autowired
    private GanpatiRepository ganpatiRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ModelMapper modelMapper;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public ApiResponse<List<GanpatiResponseDto>> getAllGanpati() {
        List<Ganpati> ganpatiList = ganpatiRepository.findByIsActiveTrue();
        List<GanpatiResponseDto> responseList = ganpatiList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(responseList);
    }

    public ApiResponse<List<GanpatiResponseDto>> getFeaturedGanpati() {
        List<Ganpati> featured = ganpatiRepository.findFeaturedGanpati();
        List<GanpatiResponseDto> responseList = featured.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(responseList);
    }

    public ApiResponse<GanpatiResponseDto> getGanpatiById(String id) {
        Optional<Ganpati> ganpati = ganpatiRepository.findById(id);
        if (ganpati.isEmpty()) {
            return ApiResponse.error("Ganpati not found");
        }
        return ApiResponse.success(mapToResponse(ganpati.get()));
    }

    public ApiResponse<GanpatiResponseDto> createGanpati(GanpatiRequestDto request) {
        try {
            User currentUser = getCurrentUser();
            Ganpati entity = mapToEntity(request);
            entity.setCreatedBy(currentUser);
            entity.setCreatedByUserEmail(currentUser.getEmail());

            List<String> imageUrls = uploadImages(request.getImages(), "ganpati_images");
            entity.setImages(imageUrls != null ? imageUrls : new ArrayList<>());
            entity.setAvailableSlots(entity.getTotalSlots());

            Ganpati saved = ganpatiRepository.save(entity);
            return ApiResponse.success(mapToResponse(saved), "Ganpati created successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to create Ganpati: " + e.getMessage());
        }
    }

    public ApiResponse<GanpatiResponseDto> updateGanpati(String id, GanpatiRequestDto request) {
        try {
            Optional<Ganpati> existingOpt = ganpatiRepository.findById(id);
            if (existingOpt.isEmpty()) {
                return ApiResponse.error("Ganpati not found");
            }

            Ganpati existing = existingOpt.get();
            updateEntity(existing, request);

            List<String> syncedImages = syncImages(
                    existing.getImages(),
                    request.getImages(),
                    request.getExistingImages(),
                    "ganpati_images"
            );
            existing.setImages(syncedImages);

            Ganpati updated = ganpatiRepository.save(existing);
            return ApiResponse.success(mapToResponse(updated), "Ganpati updated successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to update Ganpati: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Void> deleteGanpati(String id) {
        try {
            Optional<Ganpati> ganpatiOpt = ganpatiRepository.findById(id);
            if (ganpatiOpt.isEmpty()) {
                return ApiResponse.error("Ganpati not found");
            }

            Ganpati ganpati = ganpatiOpt.get();

            bookingRepository.deleteByGanpatiId(id);

            if (ganpati.getImages() != null && !ganpati.getImages().isEmpty()) {
                cloudinaryService.deleteFiles(ganpati.getImages());
            }

            ganpatiRepository.deleteById(id);
            return ApiResponse.success(null, "Ganpati deleted successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to delete Ganpati: " + e.getMessage());
        }
    }

    private List<String> uploadImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String url = cloudinaryService.uploadFile(file, folder);
                uploadedUrls.add(url);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + file.getOriginalFilename(), e);
            }
        }
        return uploadedUrls;
    }

    private List<String> syncImages(List<String> existingImages, List<MultipartFile> newFiles,
                                    List<String> existingUrlsFromRequest, String folder) {
        List<String> finalUrls = new ArrayList<>();

        if (existingUrlsFromRequest != null) {
            finalUrls.addAll(existingUrlsFromRequest);
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            List<String> newUploads = uploadImages(newFiles, folder);
            finalUrls.addAll(newUploads);
        }

        if (existingImages != null && !existingImages.isEmpty()) {
            List<String> urlsToDelete = existingImages.stream()
                    .filter(url -> !finalUrls.contains(url))
                    .collect(Collectors.toList());
            if (!urlsToDelete.isEmpty()) {
                cloudinaryService.deleteFiles(urlsToDelete);
            }
        }

        return finalUrls;
    }

    private Ganpati mapToEntity(GanpatiRequestDto dto) {
        Ganpati entity = new Ganpati();
        entity.setName(dto.getName());
        entity.setHeight(dto.getHeight());
        entity.setPrice(dto.getPrice());
        entity.setMaterial(dto.getMaterial());
        entity.setColorTheme(dto.getColorTheme());
        entity.setDescription(dto.getDescription());
        entity.setTotalSlots(dto.getTotalSlots());
        entity.setAchievements(dto.getAchievements() != null ? dto.getAchievements() : new ArrayList<>());
        entity.setIsActive(dto.getIsActive());
        return entity;
    }

    private void updateEntity(Ganpati entity, GanpatiRequestDto dto) {
        entity.setName(dto.getName());
        entity.setHeight(dto.getHeight());
        entity.setPrice(dto.getPrice());
        entity.setMaterial(dto.getMaterial());
        entity.setColorTheme(dto.getColorTheme());
        entity.setDescription(dto.getDescription());
        entity.setTotalSlots(dto.getTotalSlots());
        entity.setAchievements(dto.getAchievements() != null ? dto.getAchievements() : new ArrayList<>());
        entity.setIsActive(dto.getIsActive());
    }

    private GanpatiResponseDto mapToResponse(Ganpati entity) {
        GanpatiResponseDto response = new GanpatiResponseDto();
        modelMapper.map(entity, response);
        response.setImages(entity.getImages());
        return response;
    }
}