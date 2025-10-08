package com.tutoring.Tutorverse.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.tutoring.Tutorverse.Model.*;
import com.tutoring.Tutorverse.Repository.*;
import com.tutoring.Tutorverse.Dto.WithdrawalDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepo;

    @Autowired
    private WithdrawalRepository withdrawalRepo;

    /**
     * Create wallet if not exists
     */
    public WalletEntity getOrCreateWallet(UUID tutorId) {
        return walletRepo.findByTutorId(tutorId)
                .orElseGet(() -> {
                    WalletEntity wallet = new WalletEntity();
                    wallet.setTutorId(tutorId);
                    wallet.setAvailableBalance(0.0);
                    wallet.setUpdatedAt(LocalDateTime.now());
                    return walletRepo.save(wallet);
                });
    }

    /**
     * Get wallet details
     */
    public Optional<WalletEntity> getWallet(UUID tutorId) {
        return walletRepo.findByTutorId(tutorId);
    }

    /**
     * Request withdrawal
     */
    public String requestWithdrawal(WithdrawalDto dto) {
        WalletEntity wallet = getOrCreateWallet(dto.getTutorId());

        if (wallet.getAvailableBalance() < dto.getAmount()) {
            return "Insufficient balance";
        }

        // Deduct balance temporarily until admin processes
        wallet.setAvailableBalance(wallet.getAvailableBalance() - dto.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepo.save(wallet);

        // Record the withdrawal request
        WithdrawalEntity withdrawal = WithdrawalEntity.builder()
                .tutorId(dto.getTutorId())
                .tutorName(dto.getTutorName())
                .amount(dto.getAmount())
                .method(dto.getMethod())
                .accountName(dto.getAccountName())
                .bankName(dto.getBankName())
                .accountNumber(dto.getAccountNumber())
                .notes(dto.getNotes())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        withdrawalRepo.save(withdrawal);

        return "Withdrawal request submitted successfully";
    }

    /**
     * Get all withdrawals for a specific tutor
     */
    public Page<WithdrawalEntity> getTutorWithdrawals(UUID tutorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return withdrawalRepo.findByTutorId(tutorId, pageable);
    }

    /**
     * Admin updates withdrawal status (APPROVED / REJECTED / PAID)
     */
    public void updateWithdrawalStatus(UUID withdrawalId, String status) {
        WithdrawalEntity withdrawal = withdrawalRepo.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));

        withdrawal.setStatus(status);
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepo.save(withdrawal);
    }

    /**
     * Update tutor's wallet balance (e.g. after module purchase)
     */
    public WalletEntity updateWalletBalance(UUID tutorId, double amount) {
        WalletEntity wallet = getOrCreateWallet(tutorId);
        double creditedAmount = amount * 0.9; // 90% tutor share after 10% platform fee
        wallet.setAvailableBalance(wallet.getAvailableBalance() + creditedAmount);
        wallet.setUpdatedAt(LocalDateTime.now());

        System.out.println("Updated wallet balance for tutor " + tutorId + ": " + wallet.getAvailableBalance());
        return walletRepo.save(wallet);
    }
}
