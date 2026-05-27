package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @GetMapping("/dashboard-stats")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGanpati", 0);
        stats.put("pendingRequests", 0);
        stats.put("totalRevenue", 0);
        stats.put("pendingPayments", 0);
        stats.put("interestedUsers", 0);

        Map<String, Object> festivalAnalytics = new HashMap<>();
        festivalAnalytics.put("totalBookings", 0);
        festivalAnalytics.put("completedPickups", 0);
        festivalAnalytics.put("occupancyRate", 0);
        festivalAnalytics.put("peakHours", new ArrayList<>());
        stats.put("festivalAnalytics", festivalAnalytics);

        return ApiResponse.success(stats);
    }
}