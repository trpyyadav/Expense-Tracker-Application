package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.request.LoginRequestDTO;
import com.expensetracker.expense_tracker.dto.request.RegisterRequestDTO;
import com.expensetracker.expense_tracker.dto.response.AuthResponseDTO;
import com.expensetracker.expense_tracker.dto.response.UserResponseDTO;
import com.expensetracker.expense_tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody RegisterRequestDTO request){

        UserResponseDTO response = authService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(
            @Valid @RequestBody LoginRequestDTO request) {

        AuthResponseDTO response = authService.loginUser(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testRoute(){
        return ResponseEntity.ok("Test route is protected with jwt");
    }
}
