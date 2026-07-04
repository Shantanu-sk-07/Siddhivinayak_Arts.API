package com.suraj.MurtiSystem.controller;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.ReceiptResponseDto;
import com.suraj.MurtiSystem.service.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipt")
public class ReceiptController {

    @Autowired
    private ReceiptService receiptService;

    @PostMapping("/generate/{bookingId}")
    public ApiResponse<ReceiptResponseDto> generateReceipt(@PathVariable String bookingId) {
        return receiptService.generateReceipt(bookingId);
    }

    @GetMapping("/{token}")
    public ApiResponse<ReceiptResponseDto> getReceipt(@PathVariable String token) {
        return receiptService.getReceipt(token);
    }
}