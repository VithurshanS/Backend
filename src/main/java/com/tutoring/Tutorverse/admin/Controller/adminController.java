package com.tutoring.Tutorverse.Admin.Controller;

import com.tutoring.Tutorverse.Dto.WithdrawalDto;
import com.tutoring.Tutorverse.Model.TutorEntity;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Model.WithdrawalEntity;
import com.tutoring.Tutorverse.Repository.WithdrawalRepository;
import com.tutoring.Tutorverse.Services.UserService;
import com.tutoring.Tutorverse.Services.TutorProfileService;
import com.tutoring.Tutorverse.Services.WalletService;
import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.Admin.Services.adminService;
import com.tutoring.Tutorverse.Services.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;



@RestController
@RequestMapping("/api/admin/withdrawals")
@CrossOrigin(origins = "*")
public class adminController {

        @Autowired
        private WalletService walletService;
        @Autowired
        private UserService userService;
        @Autowired
        private TutorProfileService tutorProfileService;
        @Autowired
        private WithdrawalRepository withdrawalRepo;
        @Autowired
        private userRepository userRepo;
        @Autowired
        private adminService adminService;


    @Value("${payhere.merchantId}") private String merchantId;
    @Value("${payhere.merchantSecret}") private String merchantSecret;
    @Value("${payhere.currency}") private String currency;
    @Value("${payhere.checkoutUrl}") private String checkoutUrl;
    @Value("${payhere.returnUrl}") private String returnUrl;
    @Value("${payhere.cancelUrl}") private String cancelUrl;
    @Value("${payhere.notifyUrl}") private String notifyUrl;




    @GetMapping("/summary")
    public ResponseEntity<?> getWithdrawalSummary() {
        return ResponseEntity.ok(walletService.getWithdrawalSummary());
    }

    @GetMapping("total-pending")
    public ResponseEntity<?> getTotalPendingAmount() {
        return ResponseEntity.ok(
                Map.of("totalPendingAmount", walletService.getTotalPendingAmount())
        );
    }

    @GetMapping("/total-approved")
    public ResponseEntity<?> getTotalApprovedAmount() {
        return ResponseEntity.ok(
                Map.of("totalApprovedAmount", walletService.getTotalApprovedAmount())
        );
    }

    @GetMapping("/pending-count")
    public ResponseEntity<?> getPendingCount() {
        return ResponseEntity.ok(
                Map.of("pendingRequests", walletService.getPendingCount())
        );

    }
    @GetMapping("/get-all-withdrawals")
    public ResponseEntity<?> getAllWithdrawals() {
        try {
            Page<WithdrawalEntity> withdrawals = walletService.getAllWithdrawals(0, 10);
            return ResponseEntity.ok(withdrawals.getContent());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch withdrawals"));
        }
    }





    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateWithdrawalStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            HttpServletRequest req) {

        try {
            UUID adminId = userService.getUserIdFromRequest(req);
            walletService.updateWithdrawalStatus(id, status.toUpperCase());
            WithdrawalEntity withdrawal = withdrawalRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
            withdrawal.setAdminId(adminId);
            withdrawalRepo.save(withdrawal);
            if (status.equalsIgnoreCase("REJECTED")) {
                // Refund the amount to tutor's wallet
                walletService.updateWalletBalance(withdrawal.getTutorId(), withdrawal.getAmount());
            }

            return ResponseEntity.ok(Map.of("message", "Withdrawal " + status.toUpperCase()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update withdrawal status"));
        }
    }


    @PostMapping("/pay/{id}")
    public ResponseEntity<?> payWithdrawal(@PathVariable UUID id) {
        try {
            // Call the service method you already wrote
            Map<String, Object> paymentData = adminService.withdrawPay(id);

            return ResponseEntity.ok(paymentData);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process payment request"));
        }
    }





}