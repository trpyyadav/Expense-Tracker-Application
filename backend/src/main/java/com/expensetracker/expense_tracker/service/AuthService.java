package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.request.LoginRequestDTO;
import com.expensetracker.expense_tracker.dto.request.RegisterRequestDTO;
import com.expensetracker.expense_tracker.dto.response.AuthResponseDTO;
import com.expensetracker.expense_tracker.dto.response.UserResponseDTO;
import com.expensetracker.expense_tracker.entity.User;
import com.expensetracker.expense_tracker.exception.DuplicateEmailException;
import com.expensetracker.expense_tracker.mapper.UserMapper;
import com.expensetracker.expense_tracker.repository.UserRepository;
import com.expensetracker.expense_tracker.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponseDTO registerUser(RegisterRequestDTO req){
        if(userRepository.existsByEmail(req.getEmail())){
            throw new DuplicateEmailException("Email is already registered...");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        userRepository.save(user);

        return UserMapper.toDTO(user);
    }

    public AuthResponseDTO loginUser(LoginRequestDTO requestDTO){

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    requestDTO.getEmail(),
                    requestDTO.getPassword()
            )
        );

        // Fetch authenticated user
        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponseDTO(
                token,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
