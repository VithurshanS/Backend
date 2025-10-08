package com.tutoring.Tutorverse.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalDto {
    private UUID tutorId;
    private String tutorName;
    private double amount;
    private String method;
    private String accountName;
    private String bankName;
    private String accountNumber;
    private String notes;
}