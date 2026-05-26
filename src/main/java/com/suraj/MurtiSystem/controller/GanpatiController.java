package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.entity.Ganpati;
import com.suraj.MurtiSystem.service.GanpatiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/ganpati")
@CrossOrigin(origins = "*")
public class GanpatiController {

    @Autowired
    private GanpatiService ganpatiService;

    @GetMapping("/all")
    public ApiResponse<List<Ganpati>> getAllGanpati() {
        return ganpatiService.getAllGanpati();
    }

    @GetMapping("/featured")
    public ApiResponse<List<Ganpati>> getFeaturedGanpati() {
        return ganpatiService.getFeaturedGanpati();
    }

    @GetMapping("/{id}")
    public ApiResponse<Ganpati> getGanpatiById(@PathVariable String id) {
        return ganpatiService.getGanpatiById(id);
    }
}