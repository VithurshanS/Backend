package com.tutoring.Tutorverse.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.tutoring.Tutorverse.Model.*;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.transaction.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class PaymentRepoTest extends BaseRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ModulesRepository modulesRepository;

    @Autowired
    private TutorProfileRepository tutorProfileRepository;

    @Autowired
    private DomainRepository domainRepository;

    private StudentEntity testStudent;
    private ModuelsEntity testModule;
    private DomainEntity testDomain;
    private TutorEntity testTutor;

    @BeforeEach
    public void setUp() {
        createStandardRoles();
        
        // Create test student
        User studentUser = persistTestStudent("teststudent@example.com", "Test Student");
        entityManager.flush();

        testStudent = StudentEntity.builder()
            .user(studentUser)
            .firstName("Test")
            .lastName("Student")
            .address("123 Test St")
            .city("Test City")
            .country("Test Country")
            .phoneNumber("1234567890")
            .bio("Test student bio")
            .isActive(true)
            .build();
        testStudent = studentProfileRepository.save(testStudent);
        entityManager.flush();

        // Create test tutor
        User tutorUser = persistTestTutor("testtutor@example.com", "Test Tutor");
        entityManager.flush();

        testTutor = TutorEntity.builder()
            .user(tutorUser)
            .firstName("Test")
            .lastName("Tutor")
            .address("123 Test St")
            .city("Test City")
            .country("Test Country")
            .gender(TutorEntity.Gender.MALE)
            .phoneNo("1234567891")
            .bio("Test tutor bio")
            .build();
        testTutor = tutorProfileRepository.save(testTutor);
        entityManager.flush();

        // Create test domain
        testDomain = DomainEntity.builder()
            .name("Mathematics")
            .build();
        testDomain = domainRepository.save(testDomain);
        entityManager.flush();

        // Create test module
        testModule = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .duration(Duration.ofHours(1))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();
        testModule = modulesRepository.save(testModule);
        entityManager.flush();
    }

    @Test
    public void testCreatePayment() {
        PaymentEntity payment = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(50.00)
            .currency("LKR")
            .orderId("ORDER_123456")
            .status("PENDING")
            .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);
        entityManager.flush();

        assertThat(savedPayment.getPaymentId()).isNotNull();
        assertThat(savedPayment.getStudentId()).isEqualTo(testStudent.getStudentId());
        assertThat(savedPayment.getModuleId()).isEqualTo(testModule.getModuleId());
        assertThat(savedPayment.getAmount()).isEqualTo(50.00);
        assertThat(savedPayment.getCurrency()).isEqualTo("LKR");
        assertThat(savedPayment.getOrderId()).isEqualTo("ORDER_123456");
        assertThat(savedPayment.getStatus()).isEqualTo("PENDING");
        assertThat(savedPayment.getCreatedAt()).isNotNull();
        assertThat(savedPayment.getUpdatedAt()).isNotNull();
    }

    @Test
    public void testCreatePaymentWithDefaults() {
        PaymentEntity payment = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(50.00)
            .orderId("ORDER_123456")
            .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);
        entityManager.flush();

        // Test default values
        assertThat(savedPayment.getCurrency()).isEqualTo("LKR");
        assertThat(savedPayment.getStatus()).isEqualTo("PENDING");
        assertThat(savedPayment.getCreatedAt()).isNotNull();
        assertThat(savedPayment.getUpdatedAt()).isNotNull();
    }

    @Test
    public void testPaymentWithMissingMandatoryFields() {
        // Test missing studentId
        PaymentEntity paymentWithoutStudentId = PaymentEntity.builder()
            .moduleId(testModule.getModuleId())
            .amount(50.00)
            .orderId("ORDER_123456")
            .build();

        assertThatThrownBy(() -> {
            paymentRepository.save(paymentWithoutStudentId);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);

        // Test missing moduleId
        PaymentEntity paymentWithoutModuleId = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .amount(50.00)
            .orderId("ORDER_123456")
            .build();

        assertThatThrownBy(() -> {
            paymentRepository.save(paymentWithoutModuleId);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);

        // Test missing amount
        PaymentEntity paymentWithoutAmount = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .orderId("ORDER_123456")
            .build();

        assertThatThrownBy(() -> {
            paymentRepository.save(paymentWithoutAmount);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);

        // Test missing orderId
        PaymentEntity paymentWithoutOrderId = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(50.00)
            .build();

        assertThatThrownBy(() -> {
            paymentRepository.save(paymentWithoutOrderId);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testFindByOrderId() {
        PaymentEntity payment = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(75.00)
            .orderId("ORDER_UNIQUE_123")
            .status("COMPLETED")
            .payherePaymentId("PAYHERE_123")
            .payhereSignature("signature_hash")
            .build();

        paymentRepository.save(payment);
        entityManager.flush();

        Optional<PaymentEntity> foundPayment = paymentRepository.findByOrderId("ORDER_UNIQUE_123");
        
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getAmount()).isEqualTo(75.00);
        assertThat(foundPayment.get().getStatus()).isEqualTo("COMPLETED");
        assertThat(foundPayment.get().getPayherePaymentId()).isEqualTo("PAYHERE_123");
        assertThat(foundPayment.get().getPayhereSignature()).isEqualTo("signature_hash");

        // Test with non-existent order ID
        Optional<PaymentEntity> notFound = paymentRepository.findByOrderId("NON_EXISTENT_ORDER");
        assertThat(notFound).isEmpty();
    }

    @Test
    public void testDuplicateOrderId() {
        PaymentEntity payment1 = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(50.00)
            .orderId("DUPLICATE_ORDER_123")
            .build();

        paymentRepository.save(payment1);
        entityManager.flush();

        // Try to create another payment with the same order ID
        PaymentEntity payment2 = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(75.00)
            .orderId("DUPLICATE_ORDER_123") // Same order ID
            .build();

        // This should fail due to unique constraint on order_id
        assertThatThrownBy(() -> {
            paymentRepository.save(payment2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testPaymentStatusValues() {
        // Test different status values
        String[] statuses = {"PENDING", "COMPLETED", "FAILED", "CANCELLED", "REFUNDED"};
        
        for (int i = 0; i < statuses.length; i++) {
            PaymentEntity payment = PaymentEntity.builder()
                .studentId(testStudent.getStudentId())
                .moduleId(testModule.getModuleId())
                .amount(50.00 + i * 10)
                .orderId("ORDER_STATUS_" + i)
                .status(statuses[i])
                .build();

            PaymentEntity savedPayment = paymentRepository.save(payment);
            entityManager.flush();

            assertThat(savedPayment.getStatus()).isEqualTo(statuses[i]);
        }
    }

    @Test
    public void testPaymentCurrencyValues() {
        // Test different currency values
        String[] currencies = {"LKR", "USD", "EUR", "GBP"};
        
        for (int i = 0; i < currencies.length; i++) {
            PaymentEntity payment = PaymentEntity.builder()
                .studentId(testStudent.getStudentId())
                .moduleId(testModule.getModuleId())
                .amount(50.00 + i * 10)
                .orderId("ORDER_CURRENCY_" + i)
                .currency(currencies[i])
                .build();

            PaymentEntity savedPayment = paymentRepository.save(payment);
            entityManager.flush();

            assertThat(savedPayment.getCurrency()).isEqualTo(currencies[i]);
        }
    }

    @Test
    public void testUpdatePaymentStatus() {
        PaymentEntity payment = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(100.00)
            .orderId("ORDER_UPDATE_123")
            .status("PENDING")
            .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);
        entityManager.flush();

        // Update payment status to completed
        savedPayment.setStatus("COMPLETED");
        savedPayment.setPayherePaymentId("PAYHERE_COMPLETED_123");
        savedPayment.setPayhereSignature("completed_signature_hash");
        savedPayment.setUpdatedAt(LocalDateTime.now());

        PaymentEntity updatedPayment = paymentRepository.save(savedPayment);
        entityManager.flush();

        assertThat(updatedPayment.getStatus()).isEqualTo("COMPLETED");
        assertThat(updatedPayment.getPayherePaymentId()).isEqualTo("PAYHERE_COMPLETED_123");
        assertThat(updatedPayment.getPayhereSignature()).isEqualTo("completed_signature_hash");
        assertThat(updatedPayment.getUpdatedAt()).isAfter(updatedPayment.getCreatedAt());
    }

    @Test
    public void testPaymentWithPayhereDetails() {
        PaymentEntity payment = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(125.50)
            .orderId("ORDER_PAYHERE_123")
            .status("COMPLETED")
            .payherePaymentId("PAYHERE_987654321")
            .payhereSignature("abcd1234567890signature")
            .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);
        entityManager.flush();

        assertThat(savedPayment.getPayherePaymentId()).isEqualTo("PAYHERE_987654321");
        assertThat(savedPayment.getPayhereSignature()).isEqualTo("abcd1234567890signature");
    }

    @Test
    public void testMultiplePaymentsForSameStudentModule() {
        // Test that multiple payments can exist for the same student-module combination
        // (e.g., failed payment followed by successful payment)
        
        PaymentEntity payment1 = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(50.00)
            .orderId("ORDER_FAILED_123")
            .status("FAILED")
            .build();

        PaymentEntity payment2 = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(50.00)
            .orderId("ORDER_SUCCESS_123")
            .status("COMPLETED")
            .build();

        PaymentEntity savedPayment1 = paymentRepository.save(payment1);
        PaymentEntity savedPayment2 = paymentRepository.save(payment2);
        entityManager.flush();

        assertThat(savedPayment1.getStatus()).isEqualTo("FAILED");
        assertThat(savedPayment2.getStatus()).isEqualTo("COMPLETED");
        assertThat(savedPayment1.getOrderId()).isNotEqualTo(savedPayment2.getOrderId());
    }

    @Test
    public void testDeletePayment() {
        PaymentEntity payment = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(50.00)
            .orderId("ORDER_DELETE_123")
            .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);
        entityManager.flush();

        UUID paymentId = savedPayment.getPaymentId();
        String orderId = savedPayment.getOrderId();
        
        paymentRepository.delete(savedPayment);
        entityManager.flush();

        Optional<PaymentEntity> deletedPayment = paymentRepository.findById(paymentId);
        assertThat(deletedPayment).isEmpty();

        Optional<PaymentEntity> deletedByOrderId = paymentRepository.findByOrderId(orderId);
        assertThat(deletedByOrderId).isEmpty();
    }

    @Test
    public void testPaymentAmountPrecision() {
        // Test different amount values with decimal precision
        Double[] amounts = {10.00, 25.50, 99.99, 1000.00, 0.01};
        
        for (int i = 0; i < amounts.length; i++) {
            PaymentEntity payment = PaymentEntity.builder()
                .studentId(testStudent.getStudentId())
                .moduleId(testModule.getModuleId())
                .amount(amounts[i])
                .orderId("ORDER_AMOUNT_" + i)
                .build();

            PaymentEntity savedPayment = paymentRepository.save(payment);
            entityManager.flush();

            assertThat(savedPayment.getAmount()).isEqualTo(amounts[i]);
        }
    }

    @Test
    public void testPaymentLazyLoadingRelationships() {
        PaymentEntity payment = PaymentEntity.builder()
            .studentId(testStudent.getStudentId())
            .moduleId(testModule.getModuleId())
            .amount(50.00)
            .orderId("ORDER_LAZY_123")
            .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);
        entityManager.flush();
        entityManager.clear(); // Clear the persistence context

        // Reload the payment
        Optional<PaymentEntity> reloadedPayment = paymentRepository.findById(savedPayment.getPaymentId());
        assertThat(reloadedPayment).isPresent();

        // Test that lazy relationships can be accessed (if properly configured)
        PaymentEntity payment1 = reloadedPayment.get();
        assertThat(payment1.getStudentId()).isEqualTo(testStudent.getStudentId());
        assertThat(payment1.getModuleId()).isEqualTo(testModule.getModuleId());
        
        // Note: Accessing student and module entities would require transaction context
        // These are just testing the ID fields which are not lazy
    }
}