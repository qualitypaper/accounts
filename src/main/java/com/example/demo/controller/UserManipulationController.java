package com.example.demo.controller;

import com.example.demo.controller.dto.ForgetRequest;
import com.example.demo.controller.dto.UpdatePasswordRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@AllArgsConstructor
@RestController
public class UserManipulationController {

    private final UserService userService;

    @PostMapping("/reset")
    public ResponseEntity<?> forgetPassword(@RequestBody ForgetRequest request){
        return userService.forgetPassword(request);
    }

    @PutMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request){
        return userService.updatePassword(request);
    }


    @GetMapping("/getAll")
    public List<User> getAll(){
        return userService.getAll();
    }
}
