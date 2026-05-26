package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.entity.Ganpati;
import com.suraj.MurtiSystem.repository.GanpatiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GanpatiService {

    @Autowired
    private GanpatiRepository ganpatiRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public ApiResponse<List<Ganpati>> getAllGanpati() {
        List<Ganpati> ganpatiList = ganpatiRepository.findByIsActiveTrue();
        return ApiResponse.success(ganpatiList);
    }

    public ApiResponse<List<Ganpati>> getFeaturedGanpati() {
        List<Ganpati> featured = ganpatiRepository.findFeaturedGanpati();
        return ApiResponse.success(featured);
    }

    public ApiResponse<Ganpati> getGanpatiById(String id) {
        Optional<Ganpati> ganpati = ganpatiRepository.findById(id);
        if (ganpati.isEmpty()) {
            return ApiResponse.error("Ganpati not found");
        }
        return ApiResponse.success(ganpati.get());
    }

    public ApiResponse<Ganpati> createGanpati(Ganpati ganpati, List<MultipartFile> imageFiles) {
        try {
            List<String> imageUrls = new ArrayList<>();
            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (MultipartFile file : imageFiles) {
                    String imageUrl = cloudinaryService.uploadFile(file, "ganpati_images");
                    imageUrls.add(imageUrl);
                }
            }
            ganpati.setImages(imageUrls);
            ganpati.setAvailableSlots(ganpati.getTotalSlots());
            Ganpati saved = ganpatiRepository.save(ganpati);
            return ApiResponse.success(saved, "Ganpati created successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to upload images: " + e.getMessage());
        }
    }

    public ApiResponse<Ganpati> updateGanpati(String id, Ganpati ganpatiDetails, List<MultipartFile> imageFiles) {
        Optional<Ganpati> existingOpt = ganpatiRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ApiResponse.error("Ganpati not found");
        }

        Ganpati existing = existingOpt.get();

        if (imageFiles != null && !imageFiles.isEmpty()) {
            cloudinaryService.deleteFiles(existing.getImages());
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                String imageUrl = cloudinaryService.uploadFile(file, "ganpati_images");
                imageUrls.add(imageUrl);
            }
            existing.setImages(imageUrls);
        }

        existing.setName(ganpatiDetails.getName());
        existing.setHeight(ganpatiDetails.getHeight());
        existing.setPrice(ganpatiDetails.getPrice());
        existing.setMaterial(ganpatiDetails.getMaterial());
        existing.setColorTheme(ganpatiDetails.getColorTheme());
        existing.setDescription(ganpatiDetails.getDescription());
        existing.setTotalSlots(ganpatiDetails.getTotalSlots());
        existing.setAchievements(ganpatiDetails.getAchievements());
        existing.setIsActive(ganpatiDetails.getIsActive());

        Ganpati updated = ganpatiRepository.save(existing);
        return ApiResponse.success(updated, "Ganpati updated successfully");
    }

    public ApiResponse<Void> deleteGanpati(String id) {
        Optional<Ganpati> ganpatiOpt = ganpatiRepository.findById(id);
        if (ganpatiOpt.isEmpty()) {
            return ApiResponse.error("Ganpati not found");
        }

        Ganpati ganpati = ganpatiOpt.get();
        if (ganpati.getImages() != null && !ganpati.getImages().isEmpty()) {
            cloudinaryService.deleteFiles(ganpati.getImages());
        }

        ganpatiRepository.deleteById(id);
        return ApiResponse.success(null, "Ganpati deleted successfully");
    }
}