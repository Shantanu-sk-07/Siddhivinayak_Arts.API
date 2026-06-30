package com.suraj.MurtiSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class GanpatiRequestDto {
    @NotBlank(message = "Ganpati name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Height is required")
    private String height;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "100", message = "Price must be at least 100")
    private Double price;

    @NotBlank(message = "Material is required")
    private String material;

    @NotBlank(message = "Color theme is required")
    private String colorTheme;

    @NotNull(message = "Total slots is required")
    @Min(value = 1, message = "Total slots must be at least 1")
    private Integer totalSlots;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    private List<MultipartFile> images;
    private List<String> existingImages;
}