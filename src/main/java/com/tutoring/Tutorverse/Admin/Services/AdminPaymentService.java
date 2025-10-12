package com.tutoring.Tutorverse.Admin.Services;

import com.tutoring.Tutorverse.Repository.EnrollRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;
import com.tutoring.Tutorverse.Admin.Repository.AdminPaymentRepository;
import com.tutoring.Tutorverse.Repository.WithdrawalRepository;
import com.tutoring.Tutorverse.Repository.TutorProfileRepository;
import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.Services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminPaymentService {
    private final AdminPaymentRepository paymentRepo;

    public AdminPaymentService(AdminPaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;

    }

    public double getTotalPlatformRevenue() {
        return paymentRepo.getTotalPlatformRevenue();
    }
}




