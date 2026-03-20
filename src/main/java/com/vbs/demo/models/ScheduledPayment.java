package com.vbs.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "scheduled_payment")
public class ScheduledPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int userId;

    @Column(nullable = false)
    private String recipientUsername;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String frequency;

    @Column(nullable = false)
    private int dayOfMonth;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastRun;
    private LocalDateTime nextRun;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}