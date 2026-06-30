package com.suraj.MurtiSystem.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerRegisterResponse {
    private String id;
    private String name;
    private String phone;
    private String alternatePhone;
    private String registrationType;
    private String mandalName;
    private String adhyakshyaName;
    private String adhyakshyaPhone;
    private String address;
    private String city;
    private String taluka;
    private String district;
    private String state;
    private String pincode;
    private List<ContactPersonDto> contactPersons;
    private String role;
    private Boolean isActive;
    private Boolean isPromoted;
    private LocalDateTime createdAt;
    private String ganpatiId;
    private String ganpatiName;
    private String ganpatiHeight;
    private Double ganpatiPrice;
    private String ganpatiImage;
    private String message;
    private String status;

    @Data
    public static class ContactPersonDto {
        private String name;
        private String phone;
        private String designation;
    }
}