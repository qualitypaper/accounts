package com.example.demo.controller;

import com.example.demo.config.jwt.JwtResponse;
import com.example.demo.controller.dto.AuthenticationRequest;
import com.example.demo.controller.dto.ForgetRequest;
import com.example.demo.controller.dto.RegisterRequest;
import com.example.demo.controller.dto.UpdatePasswordRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request){
        return userService.register(request);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JwtResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest){
        return userService.authenticate(authenticationRequest);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmAccount(@RequestParam String token){
        return userService.setConfirmed(token);
    }



}
