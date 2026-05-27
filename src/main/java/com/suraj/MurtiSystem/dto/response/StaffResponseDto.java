package com.suraj.MurtiSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaffResponseDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String assignedCounter;
    private Boolean isActive;
    private LocalDateTime createdAt;
}