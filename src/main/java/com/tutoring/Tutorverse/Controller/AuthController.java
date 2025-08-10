package com.tutoring.Tutorverse.Controller;


import com.tutoring.Tutorverse.Model.roleDto;
import com.tutoring.Tutorverse.Model.userDto;
import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.Services.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    @Autowired
    private userRepository userRepo;

    @Autowired
    private JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/home")
    public String home(Authentication authentication) {
        return "home";
    }



    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (userRepo.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        userDto user = new userDto();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProviderid(null); // unused for local users
        user.setRole(roleDto.TUTOR); // set a default role instead of null
        user.setName(body.get("name")); // get name from request body if provided

        userRepo.save(user);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        Optional<userDto> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty() || userOpt.get().getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No credentials");
        }

        userDto user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtUtil.generateToken(email);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
