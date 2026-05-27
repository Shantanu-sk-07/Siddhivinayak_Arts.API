package com.suraj.MurtiSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GanpatiResponseDto {
    private String id;
    private String name;
    private String height;
    private Double price;
    private String material;
    private String colorTheme;
    private String description;
    private List<String> images;
    private Integer totalSlots;
    private Integer availableSlots;
    private Double rating;
    private List<String> achievements;
    private Boolean isActive;
    private String createdAt;
}