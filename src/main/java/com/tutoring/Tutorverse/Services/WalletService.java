package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Model.*;
import com.tutoring.Tutorverse.Repository.*;
import com.tutoring.Tutorverse.Dto.WithdrawalDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WalletService {

    @Autowired private WalletRepository walletRepo;
    @Autowired private WithdrawalRepository withdrawalRepo;

    // Create wallet if not exists
    public WalletEntity getOrCreateWallet(UUID tutorId) {
        return walletRepo.findByTutorId(tutorId)
                .orElseGet(() -> {
                    WalletEntity wallet = new WalletEntity();
                    wallet.setTutorId(tutorId);
                    wallet.setAvailableBalance(0.0);
                    return walletRepo.save(wallet);
                });
    }

    // Get wallet details
    public Optional<WalletEntity> getWallet(UUID tutorId) {
        return walletRepo.findByTutorId(tutorId);
    }

    // Withdraw funds
    public String requestWithdrawal(WithdrawalDto dto) {
        WalletEntity wallet = getOrCreateWallet(dto.getTutorId());
        if (wallet.getAvailableBalance() < dto.getAmount()) {
            return "Insufficient balance";
        }

        // Deduct temporarily
        wallet.setAvailableBalance(wallet.getAvailableBalance() - dto.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepo.save(wallet);

        // Record withdrawal
        WithdrawalEntity withdrawal = new WithdrawalEntity();
        withdrawal.setTutorId(dto.getTutorId());
        withdrawal.setAmount(dto.getAmount());
        withdrawal.setMethod(dto.getMethod());
        withdrawal.setAccountDetails(dto.getAccountDetails());
        withdrawal.setStatus("PENDING");
        withdrawalRepo.save(withdrawal);

        return "Withdrawal request submitted successfully";
    }

    // Get all withdrawals for tutor
    public List<WithdrawalEntity> getTutorWithdrawals(UUID tutorId) {
        return withdrawalRepo.findByTutorId(tutorId);
    }

    // Admin can approve withdrawal
    public void updateWithdrawalStatus(UUID withdrawalId, String status) {
        WithdrawalEntity withdrawal = withdrawalRepo.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        withdrawal.setStatus(status);
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepo.save(withdrawal);
    }
}