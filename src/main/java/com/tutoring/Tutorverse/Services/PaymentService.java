package com.tutoring.Tutorverse.Services;
import com.tutoring.Tutorverse.Model.ModuelsEntity;

import com.tutoring.Tutorverse.Model.EnrollmentEntity;
import com.tutoring.Tutorverse.Model.PaymentEntity;
import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Repository.EnrollRepository;
import com.tutoring.Tutorverse.Repository.PaymentRepository;
import com.tutoring.Tutorverse.Repository.StudentProfileRepository;
import com.tutoring.Tutorverse.SecurityConfigs.PayHereHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final EnrollRepository enrollmentRepo;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Value("${payhere.merchantId}") private String merchantId;
    @Value("${payhere.merchantSecret}") private String merchantSecret;
    @Value("${payhere.currency}") private String currency;
    @Value("${payhere.checkoutUrl}") private String checkoutUrl;
    @Value("${payhere.returnUrl}") private String returnUrl;
    @Value("${payhere.cancelUrl}") private String cancelUrl;
    @Value("${payhere.notifyUrl}") private String notifyUrl;

    public PaymentService(PaymentRepository paymentRepo, EnrollRepository enrollmentRepo) {
        this.paymentRepo = paymentRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    public Map<String, Object> createPayment(UUID studentId, UUID moduleId, double amount) {
        String orderId = UUID.randomUUID().toString();

        PaymentEntity payment = new PaymentEntity();
        payment.setStudentId(studentId);
        payment.setModuleId(moduleId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setOrderId(orderId);
        payment.setStatus("PENDING");
        paymentRepo.save(payment);

        String hash = PayHereHash.buildHash(merchantId, orderId, amount, currency, merchantSecret);
        System.out.println(hash);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("checkoutUrl", checkoutUrl);
        payload.put("merchant_id", merchantId);
        payload.put("return_url", returnUrl+ "/" + moduleId);
        payload.put("cancel_url", cancelUrl);
        payload.put("notify_url", notifyUrl);
        payload.put("order_id", orderId);
        payload.put("items", "Module Purchase");
        payload.put("currency", currency);
        payload.put("amount", PayHereHash.formatAmount(amount));

        // Get real student data from database
        try {
            StudentEntity student = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

            payload.put("first_name", student.getFirstName());
            payload.put("last_name", student.getLastName());
            payload.put("email", student.getUser().getEmail()); // Get email from User entity
            payload.put("phone", "0771234567"); // Default phone if not available in student entity
            payload.put("address", student.getAddress());
            payload.put("city", student.getCity());
            payload.put("country", student.getCountry());
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

    @Transactional
    public boolean processNotification(Map<String, String> form) {
        try {
            System.out.println("=== PAYMENT NOTIFICATION PROCESSING ===");
            System.out.println("Received form data: " + form);

            // Extract required fields
            String merchantId = form.get("merchant_id");
            String orderId = form.get("order_id");
            String payhereAmount = form.get("payhere_amount");
            String payhereCurrency = form.get("payhere_currency");
            String statusCode = form.get("status_code");
            String receivedSignature = form.get("md5sig");
            String paymentId = form.get("payment_id");

            // Validate required fields
            if (merchantId == null || orderId == null || payhereAmount == null ||
                payhereCurrency == null || statusCode == null || receivedSignature == null) {
                System.out.println("Missing required fields in notification");
                return false;
            }

            System.out.println("Status Code: " + statusCode);
            System.out.println("Merchant ID: " + merchantId);
            System.out.println("Order ID: " + orderId);
            System.out.println("Amount: " + payhereAmount);
            System.out.println("Currency: " + payhereCurrency);
            System.out.println("Received Signature: " + receivedSignature);

            String localSig = PayHereHash.buildNotifyHash(
                    merchantId, orderId, payhereAmount, payhereCurrency, statusCode, merchantSecret
            );


            System.out.println("Local Signature: " + localSig);
            System.out.println("Signatures Match: " + localSig.equals(receivedSignature));

            // Verify signature and check if payment is successful
            if (localSig.equals(receivedSignature)) {
                System.out.println("Signature verification passed");

                // Find payment by order ID
                Optional<PaymentEntity> paymentOpt = paymentRepo.findByOrderId(orderId);
                if (paymentOpt.isEmpty()) {
                    System.out.println("Payment not found for order ID: " + orderId);
                    return false;
                }

                PaymentEntity payment = paymentOpt.get();
                System.out.println("Found payment: " + payment.getPaymentId());

                // Handle different status codes
                switch (statusCode) {
                    case "2": // Success
                        System.out.println("Payment successful - updating status");
                        payment.setStatus("SUCCESS");
                        payment.setPayherePaymentId(paymentId);
                        payment.setPayhereSignature(receivedSignature);
                        payment.setUpdatedAt(LocalDateTime.now());
                        paymentRepo.save(payment);

                        // Unlock enrollment
                        Optional<EnrollmentEntity> enrollmentOpt = enrollmentRepo
                            .findByStudentStudentIdAndModuleModuleId(payment.getStudentId(), payment.getModuleId());

                        if (enrollmentOpt.isPresent()) {
                            EnrollmentEntity enrollment = enrollmentOpt.get();
                            enrollment.setPaid(true);
                            enrollmentRepo.save(enrollment);
                            System.out.println("Enrollment unlocked for student: " + payment.getStudentId());
                        } else {
                            System.out.println("No enrollment found for student: " + payment.getStudentId() +
                                            ", module: " + payment.getModuleId());
                        }
                        return true;

                    case "0": // Pending
                        System.out.println("Payment pending");
                        payment.setStatus("PENDING");
                        payment.setPayherePaymentId(paymentId);
                        payment.setPayhereSignature(receivedSignature);
                        payment.setUpdatedAt(LocalDateTime.now());
                        paymentRepo.save(payment);
                        return true;

                    case "-1": // Canceled
                        System.out.println("Payment canceled");
                        payment.setStatus("CANCELED");
                        payment.setPayherePaymentId(paymentId);
                        payment.setPayhereSignature(receivedSignature);
                        payment.setUpdatedAt(LocalDateTime.now());
                        paymentRepo.save(payment);
                        return false;

                    case "-2": // Failed
                        System.out.println("Payment failed");
                        payment.setStatus("FAILED");
                        payment.setPayherePaymentId(paymentId);
                        payment.setPayhereSignature(receivedSignature);
                        payment.setUpdatedAt(LocalDateTime.now());
                        paymentRepo.save(payment);
                        return false;

                    case "-3": // Charged back
                        System.out.println("Payment charged back");
                        payment.setStatus("CHARGED_BACK");
                        payment.setPayherePaymentId(paymentId);
                        payment.setPayhereSignature(receivedSignature);
                        payment.setUpdatedAt(LocalDateTime.now());
                        paymentRepo.save(payment);

                        // Lock enrollment if it was previously unlocked
                        enrollmentRepo.findByStudentStudentIdAndModuleModuleId(payment.getStudentId(), payment.getModuleId())
                            .ifPresent(enrollment -> {
                                enrollment.setPaid(false);
                                enrollmentRepo.save(enrollment);
                                System.out.println("Enrollment locked due to chargeback");
                            });
                        return false;

                    default:
                        System.out.println("Unknown status code: " + statusCode);
                        return false;
                }
            } else {
                System.out.println("Signature verification failed");
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error processing payment notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
