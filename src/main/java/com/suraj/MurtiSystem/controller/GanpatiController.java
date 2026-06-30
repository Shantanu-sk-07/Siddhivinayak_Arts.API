package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.GanpatiResponseDto;
import com.suraj.MurtiSystem.service.GanpatiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ganpati")
public class GanpatiController {
    @Autowired
    private GanpatiService ganpatiService;

    @GetMapping("/all")
    public ApiResponse<List<GanpatiResponseDto>> getAllGanpati() {
        return ganpatiService.getAllGanpati();
    }

    @GetMapping("/featured")
    public ApiResponse<List<GanpatiResponseDto>> getFeaturedGanpati() {
        return ganpatiService.getFeaturedGanpati();
    }

    @GetMapping("/{id}")
    public ApiResponse<GanpatiResponseDto> getGanpatiById(@PathVariable String id) {
        return ganpatiService.getGanpatiById(id);
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Object>> toggleLike(@PathVariable String id, @RequestParam String userId) {
        return ganpatiService.toggleLike(id, userId);
    }
}