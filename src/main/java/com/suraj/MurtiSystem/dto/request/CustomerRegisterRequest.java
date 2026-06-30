package com.suraj.MurtiSystem.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CustomerRegisterRequest {

    @NotBlank(message = "नाव आवश्यक आहे")
    private String name;

    @NotBlank(message = "मोबाईल नंबर आवश्यक आहे")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "वैध 10 अंकी मोबाईल नंबर प्रविष्ट करा")
    private String phone;

    private String alternatePhone;
    private String city;
    private String pincode;

    @NotBlank(message = "राज्य आवश्यक आहे")
    private String state;

    @NotBlank(message = "जिल्हा आवश्यक आहे")
    private String district;

    @NotBlank(message = "तालुका आवश्यक आहे")
    private String taluka;

    @NotBlank(message = "पत्ता आवश्यक आहे")
    private String address;

    @NotBlank(message = "नोंदणी प्रकार आवश्यक आहे")
    private String registrationType;

    private String mandalName;
    private String adhyakshyaName;
    private String adhyakshyaPhone;
    private String contactPerson1Phone;
    private String contactPerson2Phone;

    private String ganpatiId;
    private String message;
}