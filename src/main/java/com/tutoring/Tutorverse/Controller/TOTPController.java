package com.tutoring.Tutorverse.Controller;


import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.Services.JwtServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tutoring.Tutorverse.Services.TOTPService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/2fa")
public class TOTPController {

    @Autowired
    private TOTPService totpService;

    @Autowired
    private userRepository userRepo;

    @Autowired
    private JwtServices jwtServices;

    @GetMapping("/generate")
    public ResponseEntity<?> generate(HttpServletRequest request) throws Exception {
        Optional<User> userOpt = getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userOpt.get();

        String secretKey = totpService.generateSecretKey();
        // Persist secret temporarily until verified; client will call /verify to enable
        user.setTotpSecret(secretKey);
        userRepo.save(user);

        String qrImageBase64 = totpService.getQRCode(secretKey, user.getEmail());
        return ResponseEntity.ok(Map.of(
                "secretKey", secretKey,
                "qrImage", "data:image/png;base64," + qrImageBase64
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(HttpServletRequest request, @RequestParam int code) {
        Optional<User> userOpt = getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userOpt.get();
        String secretKey = user.getTotpSecret();
        if (secretKey == null || secretKey.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No TOTP secret found. Generate first.");
        }

        boolean isValid = totpService.verifyCode(secretKey, code);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid code");
        }

        user.setTwoFactorEnabled(true);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "2FA enabled"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(HttpServletRequest request) {
        Optional<User> userOpt = getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "enabled", user.isTwoFactorEnabled(),
                // expose whether secret exists but never the secret
                "hasSecret", user.getTotpSecret() != null && !user.getTotpSecret().isEmpty()
        ));
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable(HttpServletRequest request, @RequestParam(required = false) Integer code) {
        Optional<User> userOpt = getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userOpt.get();
        if (user.isTwoFactorEnabled()) {
            // If enabled, require a valid code to disable (optional if code provided)
            if (code == null || !totpService.verifyCode(user.getTotpSecret(), code)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Valid code required to disable 2FA");
            }
        }
        user.setTwoFactorEnabled(false);
        // Keep secret or clear it; here we clear to require new setup next time
        user.setTotpSecret(null);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "2FA disabled"));
    }

    private Optional<User> getUserFromRequest(HttpServletRequest req) {
        if (req.getCookies() == null) return Optional.empty();
        for (Cookie cookie : req.getCookies()) {
            if ("jwt_token".equals(cookie.getName())) {
                String token = cookie.getValue();
                if (token != null && jwtServices.validateJwtToken(token)) {
                    String email = jwtServices.getEmailFromJwtToken(token);
                    return userRepo.findByEmail(email);
                }
            }
        }
        return Optional.empty();
    }
}
