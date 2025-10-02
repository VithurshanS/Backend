package com.tutoring.Tutorverse.Dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentDto {
    private UUID studentId;
    private UUID moduleId;
    private double amount;
}
