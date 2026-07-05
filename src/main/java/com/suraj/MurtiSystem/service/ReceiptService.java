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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

            Receipt receipt = receiptRepository.findByBooking_Id(bookingId).orElse(null);

            byte[] pdfBytes = pdfGeneratorService.generateReceiptPdf(booking);
            String pdfUrl = cloudinaryService.uploadFile(pdfBytes, "receipts/" + booking.getReceiptNumber() + ".pdf");

            if (receipt == null) {
                receipt = new Receipt();
                receipt.setToken(generateSecureToken());
                receipt.setBooking(booking);
                receipt.setIsActive(true);
            }
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
            populateBookingFields(response, booking);

            return ApiResponse.success(response, "Receipt generated successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to generate receipt: " + e.getMessage());
        }
    }

    public ApiResponse<ReceiptResponseDto> getReceipt(String token) {
        try {
            Receipt receipt = receiptRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid receipt link"));

            if (!receipt.getIsActive()) {
                return ApiResponse.error("Receipt is not available.");
            }

            ConfirmedBooking booking = receipt.getBooking();

            ReceiptResponseDto response = new ReceiptResponseDto();
            response.setId(receipt.getId());
            response.setToken(receipt.getToken());
            response.setReceiptUrl(baseUrl + "/receipt/" + token);
            response.setBookingId(booking.getId());
            response.setPdfPath(receipt.getPdfPath());
            response.setCreatedDate(receipt.getCreatedDate());
            response.setIsActive(receipt.getIsActive());
            populateBookingFields(response, booking);

            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("Invalid or expired receipt link");
        }
    }

    private void populateBookingFields(ReceiptResponseDto dto, ConfirmedBooking booking) {
        dto.setReceiptNumber(booking.getReceiptNumber());
        dto.setCustomerName(booking.getCustomerName() != null ? booking.getCustomerName() :
                (booking.getCustomer() != null ? booking.getCustomer().getName() : "N/A"));
        dto.setCustomerPhone(booking.getCustomerPhone() != null ? booking.getCustomerPhone() :
                (booking.getCustomer() != null ? booking.getCustomer().getPhone() : "N/A"));
        dto.setCustomerAddress(booking.getCustomerAddress() != null ? booking.getCustomerAddress() :
                (booking.getCustomer() != null ? booking.getCustomer().getAddress() : "N/A"));
        dto.setCustomerTaluka(booking.getCustomerTaluka() != null ? booking.getCustomerTaluka() :
                (booking.getCustomer() != null ? booking.getCustomer().getTaluka() : "N/A"));
        dto.setCustomerDistrict(booking.getCustomerDistrict() != null ? booking.getCustomerDistrict() :
                (booking.getCustomer() != null ? booking.getCustomer().getDistrict() : "N/A"));
        dto.setMandalName(booking.getMandalName() != null ? booking.getMandalName() :
                (booking.getCustomer() != null ? booking.getCustomer().getMandalName() : "N/A"));

        if (booking.getGanpati() != null) {
            dto.setGanpatiName(booking.getGanpati().getName());
            dto.setGanpatiHeight(booking.getGanpati().getHeight());
            dto.setGanpatiPrice(booking.getGanpati().getPrice());
        }

        dto.setAdvancePayment(booking.getAdvancePayment());
        dto.setRemainingPayment(booking.getRemainingPayment());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setTotalPaidSoFar(booking.getTotalPaidSoFar());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStatus(booking.getStatus());

        if (booking.getPaymentHistory() != null) {
            List<ReceiptResponseDto.PaymentRecordDto> history = booking.getPaymentHistory().stream()
                    .map(record -> {
                        ReceiptResponseDto.PaymentRecordDto r = new ReceiptResponseDto.PaymentRecordDto();
                        r.setAmount(record.getAmount());
                        r.setPaymentDate(record.getPaymentDate());
                        r.setPaymentType(record.getPaymentType());
                        r.setNotes(record.getNotes());
                        r.setRemainingAfterPayment(record.getRemainingAfterPayment());
                        return r;
                    })
                    .collect(Collectors.toList());
            dto.setPaymentHistory(history);
        }
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                System.currentTimeMillis() +
                UUID.randomUUID().toString().substring(0, 8);
    }
}