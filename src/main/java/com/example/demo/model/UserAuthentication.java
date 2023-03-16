package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthentication {
    @SequenceGenerator(
            name = "user_authentication_sequence",
            sequenceName = "user_authentication_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "access_token")
    private String accessToken;
    @Column(name="last_login")
    private LocalDateTime lastLogin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    @MapsId
    private User user;

}
