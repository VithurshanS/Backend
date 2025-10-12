package com.tutoring.Tutorverse.Admin.Services;

import com.tutoring.Tutorverse.Model.*;
import com.tutoring.Tutorverse.Repository.EnrollRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;
import com.tutoring.Tutorverse.Repository.PaymentRepository;
import com.tutoring.Tutorverse.Repository.WithdrawalRepository;
import com.tutoring.Tutorverse.Repository.TutorProfileRepository;
import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.SecurityConfigs.PayHereHash;
import com.tutoring.Tutorverse.Services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class adminService {
    private final PaymentRepository paymentRepo;
    private final EnrollRepository enrollmentRepo;

    @Autowired
    private TutorProfileRepository tutorProfileRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private ModulesRepository modulesRepository;

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private userRepository userRepo;

    public adminService(PaymentRepository paymentRepo, EnrollRepository enrollmentRepo) {
        this.paymentRepo = paymentRepo;
        this.enrollmentRepo = enrollmentRepo;
    }


    @Value("${payhere.merchantId}") private String merchantId;
    @Value("${payhere.merchantSecret}") private String merchantSecret;
    @Value("${payhere.currency}") private String currency;
    @Value("${payhere.checkoutUrl}") private String checkoutUrl;
    @Value("${payhere.notifyUrl}") private String notifyUrl;

    public Map<String, Object> withdrawPay(@PathVariable UUID id) {

        WithdrawalEntity withdrawal = withdrawalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));

        Optional<User> tutor = userRepo.findById(withdrawal.getTutorId());
        Optional<TutorEntity> tutorProfile = tutorProfileRepository.findById(withdrawal.getTutorId());

        String orderId = withdrawal.getWithdrawalId().toString();
        Double amount = withdrawal.getAmount();

        String hash = PayHereHash.buildHash(merchantId, orderId, amount, currency, merchantSecret);
        System.out.println(hash);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("checkoutUrl", checkoutUrl);
        payload.put("merchant_id", merchantId);
        payload.put("return_url", "https://tutorverse.com/admin/withdrawals");
        payload.put("cancel_url", "https://tutorverse.com/admin/withdrawals");
        payload.put("notify_url", notifyUrl);
        payload.put("order_id", orderId);
        payload.put("items", "Module Purchase");
        payload.put("currency", currency);
        payload.put("amount", PayHereHash.formatAmount(amount));

        try {
        payload.put("first_name", tutorProfile.get().getFirstName());
        payload.put("last_name", tutorProfile.get().getLastName());
        payload.put("email", tutor.get().getEmail()); // Get email from User entity
        payload.put("phone", tutorProfile.get().getPhoneNo()); // Default phone if not available in student entity
        payload.put("address", tutorProfile.get().getAddress());
        payload.put("city", tutorProfile.get().getCity());
        payload.put("country", tutorProfile.get().getCountry());
        } catch (Exception e) {
            // Fallback to demo data if student not found
            payload.put("first_name", "Test");
            payload.put("last_name", "Student");
            payload.put("email", "test@example.com");
            payload.put("phone", "0771234567");
            payload.put("address", "123, Test Street");
            payload.put("city", "Colombo");
            payload.put("country", "Sri Lanka");
        }
        payload.put("hash", hash);
        return payload;
    }
}




