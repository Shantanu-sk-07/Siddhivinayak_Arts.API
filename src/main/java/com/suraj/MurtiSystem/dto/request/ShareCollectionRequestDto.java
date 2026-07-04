package com.suraj.MurtiSystem.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ShareCollectionRequestDto {
    private List<String> customerIds;
    private List<String> ganpatiIds;
    private Integer expiryDays;
}