// ConfirmedBookingRequestDto.java
package com.suraj.MurtiSystem.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class ConfirmedBookingRequestDto {
    private String customerId;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String customerVillage;
    private String customerTaluka;
    private String customerDistrict;
    private String mandalName;

    private List<ContactPersonDto> additionalContacts;

    @NotBlank(message = "गणपती ID आवश्यक आहे")
    private String ganpatiId;

    @NotNull(message = "अग्रिम पेमेंट आवश्यक आहे")
    @DecimalMin(value = "0", message = "अग्रिम पेमेंट 0 किंवा अधिक असावे")
    private Double advancePayment;

    @NotNull(message = "बाकी पेमेंट आवश्यक आहे")
    @DecimalMin(value = "0", message = "बाकी पेमेंट 0 किंवा अधिक असावे")
    private Double remainingPayment;

    @NotNull(message = "एकूण किंमत आवश्यक आहे")
    @DecimalMin(value = "0", message = "एकूण किंमत 0 किंवा अधिक असावे")
    private Double totalPrice;

    private String bookingDate;
    private String notes;
    private String status;
    private Boolean createNewCustomer;
    private String customerRegistrationType;
    private List<ContactPersonDto> customerContactPersons;

    private List<InstallmentDto> installments;

    @Data
    public static class ContactPersonDto {
        private String name;
        private String phone;
        private String designation;
    }

    @Data
    public static class InstallmentDto {
        private Integer id;
        private Double remainingAmount;
        private Double paidAmount;
        private Double newRemaining;
        private String date;
        private Boolean isFinal;
    }
}