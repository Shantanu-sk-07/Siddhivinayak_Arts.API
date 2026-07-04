package com.suraj.MurtiSystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "share_collection_customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareCollectionCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "share_collection_id", nullable = false)
    private ShareCollection shareCollection;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}