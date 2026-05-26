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
@Table(name = "ganpatis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ganpati {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String height;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String material;

    @Column(nullable = false)
    private String colorTheme;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "ganpati_images", joinColumns = @JoinColumn(name = "ganpati_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalSlots;

    private Integer availableSlots;

    private Double rating = 0.0;

    @ElementCollection
    @CollectionTable(name = "ganpati_achievements", joinColumns = @JoinColumn(name = "ganpati_id"))
    @Column(name = "achievement")
    private List<String> achievements = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (availableSlots == null) {
            availableSlots = totalSlots;
        }
    }
}