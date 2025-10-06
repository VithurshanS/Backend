package com.tutoring.Tutorverse.Controller;

import com.tutoring.Tutorverse.Dto.WithdrawalDto;
import com.tutoring.Tutorverse.Services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired private WalletService walletService;

    @GetMapping("/{tutorId}")
    public ResponseEntity<?> getWallet(@PathVariable UUID tutorId) {
        return walletService.getWallet(tutorId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawalDto dto) {
        return ResponseEntity.ok(walletService.requestWithdrawal(dto));
    }

    @GetMapping("/withdrawals/{tutorId}")
    public ResponseEntity<List<?>> getWithdrawals(@PathVariable UUID tutorId) {
        return ResponseEntity.ok(walletService.getTutorWithdrawals(tutorId));
    }

}