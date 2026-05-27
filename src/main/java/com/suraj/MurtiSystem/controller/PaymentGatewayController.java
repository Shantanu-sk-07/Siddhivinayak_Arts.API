package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentGatewayController {

    @PostMapping("/create-order")
    public ApiResponse<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("order", Map.of(
                "id", "order_" + System.currentTimeMillis(),
                "amount", request.get("amount"),
                "currency", "INR"
        ));
        return ApiResponse.success(response);
    }

    @PostMapping("/verify")
    public ApiResponse<Map<String, Object>> verifyPayment(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Payment verified successfully");
        return ApiResponse.success(response);
    }

    @GetMapping("/receipt/{paymentId}")
    public ApiResponse<Map<String, String>> getReceipt(@PathVariable String paymentId) {
        Map<String, String> response = new HashMap<>();
        response.put("url", "/api/receipts/" + paymentId + ".pdf");
        return ApiResponse.success(response);
    }
}