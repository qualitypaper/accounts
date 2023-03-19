package com.example.demo.repository;

import com.example.demo.model.AuthenticationType;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
    boolean existsUserByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.authType = ?2 WHERE u.email = ?1")
    public void updateAuthenticationType(String username, AuthenticationType authenticationType);
}
