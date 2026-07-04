package com.suraj.MurtiSystem.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShareCollectionResponseDto {
    private String id;
    private String token;
    private String shareUrl;
    private String createdBy;
    private LocalDateTime createdDate;
    private LocalDateTime expiryDate;
    private Boolean isActive;
    private List<String> ganpatiIds;
    private List<String> customerIds;
}