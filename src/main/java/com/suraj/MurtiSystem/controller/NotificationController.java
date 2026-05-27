package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getNotifications() {
        return ApiResponse.success(new ArrayList<>(), "No notifications");
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount() {
        return ApiResponse.success(0, "No unread notifications");
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        return ApiResponse.success(null, "Marked as read");
    }

    @PostMapping("/mark-all-read")
    public ApiResponse<Void> markAllAsRead() {
        return ApiResponse.success(null, "All marked as read");
    }
}