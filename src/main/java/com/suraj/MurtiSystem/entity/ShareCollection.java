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
@Table(name = "share_collection")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Owner createdBy;

    @CreationTimestamp
    private LocalDateTime createdDate;

    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "shareCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShareCollectionGanpati> ganpatis = new ArrayList<>();

    @OneToMany(mappedBy = "shareCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShareCollectionCustomer> customers = new ArrayList<>();
}