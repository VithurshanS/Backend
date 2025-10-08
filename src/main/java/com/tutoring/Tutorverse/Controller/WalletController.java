package com.tutoring.Tutorverse.Controller;

import com.tutoring.Tutorverse.Dto.WithdrawalDto;
import com.tutoring.Tutorverse.Model.TutorEntity;
import com.tutoring.Tutorverse.Services.WalletService;
import com.tutoring.Tutorverse.Services.UserService;
import com.tutoring.Tutorverse.Services.TutorProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired private WalletService walletService;
    @Autowired private UserService userService;
    @Autowired private TutorProfileService tutorProfileService;

    @GetMapping("/mywallet")
    public ResponseEntity<?> getWallet(HttpServletRequest req) {
        try {
            UUID tutorId = userService.getUserIdFromRequest(req);
            if (tutorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing authentication token");
            }
            return walletService.getWallet(tutorId)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token: " + e.getMessage());
        }
    }


    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawalDto dto, HttpServletRequest req) {
        UUID tutorId = userService.getUserIdFromRequest(req);
        TutorEntity tutor = tutorProfileService.getTutorProfile(tutorId);
        if (tutorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing authentication token");
        }
        dto.setTutorId(tutorId);
        dto.setTutorName(tutor.getFirstName() + " " + tutor.getLastName());
        return ResponseEntity.ok(walletService.requestWithdrawal(dto));
    }

    @GetMapping("/withdrawals/{tutorId}")
    public ResponseEntity<List<?>> getWithdrawals(@PathVariable UUID tutorId) {
        return ResponseEntity.ok(walletService.getTutorWithdrawals(tutorId));
    }

}