package com.tutoring.Tutorverse.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalDto {
    private String tutorName;
    private UUID tutorId;
    private double amount;
    private String method;
    private String accountDetails;
}