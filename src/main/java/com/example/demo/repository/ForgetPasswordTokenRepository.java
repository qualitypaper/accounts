package com.example.demo.repository;

import com.example.demo.model.ForgetPasswordToken;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgetPasswordTokenRepository extends JpaRepository<ForgetPasswordToken, Long> {
    Optional<ForgetPasswordToken> findByUser(User user);
    Optional<ForgetPasswordToken> findByToken(String token);
}
