package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.request.ShareCollectionRequestDto;
import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.ShareCollectionResponseDto;
import com.suraj.MurtiSystem.service.ShareService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @PostMapping("/collection")
    public ApiResponse<ShareCollectionResponseDto> createShareCollection(
            @Valid @RequestBody ShareCollectionRequestDto request,
            @RequestParam String adminId) {
        return shareService.createShareCollection(request, adminId);
    }

    @GetMapping("/collection/{token}")
    public ApiResponse<ShareCollectionResponseDto> getShareCollection(@PathVariable String token) {
        return shareService.getShareCollection(token);
    }

    @DeleteMapping("/collection/{token}")
    public ApiResponse<Void> deactivateShareCollection(@PathVariable String token) {
        return shareService.deactivateShareCollection(token);
    }
}