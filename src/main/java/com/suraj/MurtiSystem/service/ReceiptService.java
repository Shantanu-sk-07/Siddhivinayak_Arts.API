package com.suraj.MurtiSystem.service;

import com.suraj.MurtiSystem.dto.response.ApiResponse;
import com.suraj.MurtiSystem.dto.response.ReceiptResponseDto;
import com.suraj.MurtiSystem.entity.ConfirmedBooking;
import com.suraj.MurtiSystem.entity.Receipt;
import com.suraj.MurtiSystem.repository.ConfirmedBookingRepository;
import com.suraj.MurtiSystem.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ConfirmedBookingRepository bookingRepository;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ApiResponse<ReceiptResponseDto> generateReceipt(String bookingId) {
        try {
            ConfirmedBooking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            byte[] pdfBytes = pdfGeneratorService.generateReceiptPdf(booking);

            String pdfUrl = cloudinaryService.uploadFile(pdfBytes, "receipts/" + booking.getReceiptNumber() + ".pdf");

            Receipt receipt = new Receipt();
            receipt.setToken(generateSecureToken());
            receipt.setBooking(booking);
            receipt.setPdfPath(pdfUrl);
            receipt.setIsActive(true);

            Receipt saved = receiptRepository.save(receipt);

            ReceiptResponseDto response = new ReceiptResponseDto();
            response.setId(saved.getId());
            response.setToken(saved.getToken());
            response.setReceiptUrl(baseUrl + "/receipt/" + saved.getToken());
            response.setBookingId(booking.getId());
            response.setPdfPath(pdfUrl);
            response.setCreatedDate(saved.getCreatedDate());
            response.setIsActive(saved.getIsActive());

            return ApiResponse.success(response, "Receipt generated successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to generate receipt: " + e.getMessage());
        }
    }

    public ApiResponse<ReceiptResponseDto> getReceipt(String token) {
        try {
            Receipt receipt = receiptRepository.findValidByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired receipt link"));

            ReceiptResponseDto response = new ReceiptResponseDto();
            response.setId(receipt.getId());
            response.setToken(receipt.getToken());
            response.setReceiptUrl(baseUrl + "/receipt/" + token);
            response.setBookingId(receipt.getBooking().getId());
            response.setPdfPath(receipt.getPdfPath());
            response.setCreatedDate(receipt.getCreatedDate());
            response.setIsActive(receipt.getIsActive());

            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("Invalid or expired receipt link");
        }
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                System.currentTimeMillis() +
                UUID.randomUUID().toString().substring(0, 8);
    }
}