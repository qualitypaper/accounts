package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "forget_token")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ForgetPasswordToken {
    @SequenceGenerator(
            name = "forget_token_sequence",
            sequenceName = "forget_token_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String token;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "reset_at")
    private LocalDateTime resetAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    @MapsId
    private User user;
}
