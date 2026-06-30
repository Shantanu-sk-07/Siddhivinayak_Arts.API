package com.suraj.MurtiSystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String phone;

    private String alternatePhone;

    @Column(nullable = false)
    private String role = "CUSTOMER";

    @Enumerated(EnumType.STRING)
    private RegistrationType registrationType;

    private String mandalName;
    private String adhyakshyaName;
    private String adhyakshyaPhone;

    private String address;
    private String taluka;
    private String district;
    private String state;
    private String city;
    private String pincode;

    @ElementCollection
    @CollectionTable(name = "customer_contact_persons", joinColumns = @JoinColumn(name = "customer_id"))
    private List<ContactPerson> contactPersons = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isPromoted = false;

    private String ganpatiId;
    private String ganpatiName;
    private String ganpatiHeight;
    private Double ganpatiPrice;
    private String ganpatiImage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum RegistrationType {
        HOME, MANDAL
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactPerson {
        private String name;
        private String phone;
        private String designation;
    }
}