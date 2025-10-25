package com.tutoring.Tutorverse.Repository;

import org.hibernate.exception.ConstraintViolationException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tutoring.Tutorverse.Model.*;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;


@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class EnrollRepoTests extends BaseRepositoryTest {

    @Autowired
    private EnrollRepository enrollRepository;

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
    public void testCreateEnrollment() {
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(false)
            .build();

        EnrollmentEntity savedEnrollment = enrollRepository.save(enrollment);
        entityManager.flush();

        assertThat(savedEnrollment.getEnrolmentId()).isNotNull();
        assertThat(savedEnrollment.getStudent().getStudentId()).isEqualTo(testStudent.getStudentId());
        assertThat(savedEnrollment.getModule().getModuleId()).isEqualTo(testModule.getModuleId());
        assertThat(savedEnrollment.isPaid()).isFalse();
    }

    @Test
    public void testCreatePaidEnrollment() {
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(true)
            .build();

        EnrollmentEntity savedEnrollment = enrollRepository.save(enrollment);
        entityManager.flush();

        assertThat(savedEnrollment.isPaid()).isTrue();
    }

    @Test
    public void testEnrollmentWithMissingMandatoryFields() {
        // Test missing student
        EnrollmentEntity enrollmentWithoutStudent = EnrollmentEntity.builder()
            .module(testModule)
            .isPaid(false)
            .build();

        assertThatThrownBy(() -> {
            enrollRepository.save(enrollmentWithoutStudent);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test missing module
        EnrollmentEntity enrollmentWithoutModule = EnrollmentEntity.builder()
            .student(testStudent)
            .isPaid(false)
            .build();

        assertThatThrownBy(() -> {
            enrollRepository.save(enrollmentWithoutModule);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    public void testFindByStudentId() {
        EnrollmentEntity enrollment1 = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(false)
            .build();

        // Create another module for second enrollment
        ModuelsEntity module2 = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Algebra I")
            .domain(testDomain)
            .fee(new BigDecimal("45.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();
        module2 = modulesRepository.save(module2);
        entityManager.flush();

        EnrollmentEntity enrollment2 = EnrollmentEntity.builder()
            .student(testStudent)
            .module(module2)
            .isPaid(true)
            .build();

        enrollRepository.save(enrollment1);
        enrollRepository.save(enrollment2);
        entityManager.flush();

        List<EnrollmentEntity> enrollments = enrollRepository.findByStudentStudentId(testStudent.getStudentId());
        
        assertThat(enrollments).hasSize(2);
        assertThat(enrollments).extracting(EnrollmentEntity::getModule)
            .extracting(ModuelsEntity::getName)
            .containsExactlyInAnyOrder("Calculus I", "Algebra I");
    }

    @Test
    public void testExistsByStudentIdAndModuleId() {
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(false)
            .build();

        enrollRepository.save(enrollment);
        entityManager.flush();

        boolean exists = enrollRepository.existsByStudentStudentIdAndModuleModuleId(
            testStudent.getStudentId(), 
            testModule.getModuleId()
        );
        assertThat(exists).isTrue();

        boolean notExists = enrollRepository.existsByStudentStudentIdAndModuleModuleId(
            UUID.randomUUID(), 
            testModule.getModuleId()
        );
        assertThat(notExists).isFalse();
    }

    @Test
    public void testFindByStudentIdAndModuleId() {
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(true)
            .build();

        EnrollmentEntity savedEnrollment = enrollRepository.save(enrollment);
        entityManager.flush();

        Optional<EnrollmentEntity> foundEnrollment = enrollRepository.findByStudentStudentIdAndModuleModuleId(
            testStudent.getStudentId(), 
            testModule.getModuleId()
        );

        assertThat(foundEnrollment).isPresent();
        assertThat(foundEnrollment.get().getEnrolmentId()).isEqualTo(savedEnrollment.getEnrolmentId());
        assertThat(foundEnrollment.get().isPaid()).isTrue();
    }

    @Test
    public void testFindIsPaidByStudentIdAndModuleId() {
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(true)
            .build();

        enrollRepository.save(enrollment);
        entityManager.flush();

        Optional<Boolean> isPaid = enrollRepository.findIsPaidByStudentIdAndModuleId(
            testStudent.getStudentId(), 
            testModule.getModuleId()
        );

        assertThat(isPaid).isPresent();
        assertThat(isPaid.get()).isTrue();

        // Test with non-existent enrollment
        Optional<Boolean> notFound = enrollRepository.findIsPaidByStudentIdAndModuleId(
            UUID.randomUUID(), 
            testModule.getModuleId()
        );
        assertThat(notFound).isEmpty();
    }

    @Test
    public void testGetIsPaidByStudentStudentIdAndModuleModuleId() {
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(false)
            .build();

        enrollRepository.save(enrollment);
        entityManager.flush();

        Optional<Boolean> isPaid = enrollRepository.getIsPaidByStudentStudentIdAndModuleModuleId(
            testStudent.getStudentId(), 
            testModule.getModuleId()
        );

        assertThat(isPaid).isPresent();
        assertThat(isPaid.get()).isFalse();
    }

    @Test
    public void testDuplicateEnrollment() {
        // Create first enrollment
        EnrollmentEntity enrollment1 = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(false)
            .build();

        EnrollmentEntity savedEnrollment1 = enrollRepository.save(enrollment1);
        entityManager.flush();
        
        assertThat(savedEnrollment1.getEnrolmentId()).isNotNull();

        // Try to create duplicate enrollment for same student and module
        EnrollmentEntity enrollment2 = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)  // Same student, same module
            .isPaid(true)        // Different payment status but same student+module
            .build();

        // This should throw DataIntegrityViolationException due to unique constraint on (student_id, module_id)
        assertThatThrownBy(() -> {
            enrollRepository.save(enrollment2);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class)
          .hasMessageContaining("constraint");

        // Note: After a constraint violation, the transaction is aborted in PostgreSQL
        // We cannot perform additional queries in the same transaction
        // The rollback will handle cleanup automatically
    }

    @Test
    public void testNoDuplicateEnrollmentExists() {
        // Verify that we can create one enrollment and it exists
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(false)
            .build();

        EnrollmentEntity savedEnrollment = enrollRepository.save(enrollment);
        entityManager.flush();
        
        assertThat(savedEnrollment.getEnrolmentId()).isNotNull();

        // Verify exactly one enrollment exists for this student-module combination
        List<EnrollmentEntity> enrollments = enrollRepository.findByStudentStudentId(testStudent.getStudentId());
        assertThat(enrollments).hasSize(1);
        assertThat(enrollments.get(0).getEnrolmentId()).isEqualTo(savedEnrollment.getEnrolmentId());

        // Verify using exists method
        boolean exists = enrollRepository.existsByStudentStudentIdAndModuleModuleId(
            testStudent.getStudentId(), testModule.getModuleId()
        );
        assertThat(exists).isTrue();
    }

    @Test
    public void testValidEnrollmentCombinations() {
        // Test 1: Same student can enroll in different modules
        ModuelsEntity module2 = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Algebra II")
            .domain(testDomain)
            .fee(new BigDecimal("55.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();
        module2 = modulesRepository.save(module2);
        entityManager.flush();

        EnrollmentEntity enrollment1 = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)  // First module
            .isPaid(false)
            .build();

        EnrollmentEntity enrollment2 = EnrollmentEntity.builder()
            .student(testStudent)  // Same student
            .module(module2)       // Different module
            .isPaid(true)
            .build();

        enrollRepository.save(enrollment1);
        enrollRepository.save(enrollment2);
        entityManager.flush();

        // Test 2: Different students can enroll in same module
        User student2User = persistTestStudent("student2@test.com", "Student Two");
        StudentEntity student2 = StudentEntity.builder()
            .user(student2User)
            .firstName("Student")
            .lastName("Two")
            .address("456 Oak St")
            .city("Other City")
            .country("Test Country")
            .phoneNumber("0987654321")
            .isActive(true)
            .build();
        student2 = studentProfileRepository.save(student2);
        entityManager.flush();

        EnrollmentEntity enrollment3 = EnrollmentEntity.builder()
            .student(student2)     // Different student
            .module(testModule)    // Same module as enrollment1
            .isPaid(false)
            .build();

        enrollRepository.save(enrollment3);
        entityManager.flush();

        // Verify all enrollments were created successfully
        List<EnrollmentEntity> allEnrollments = enrollRepository.findAll();
        assertThat(allEnrollments).hasSize(3);

        // Verify testStudent has 2 enrollments (different modules)
        List<EnrollmentEntity> student1Enrollments = enrollRepository.findByStudentStudentId(testStudent.getStudentId());
        assertThat(student1Enrollments).hasSize(2);

        // Verify student2 has 1 enrollment
        List<EnrollmentEntity> student2Enrollments = enrollRepository.findByStudentStudentId(student2.getStudentId());
        assertThat(student2Enrollments).hasSize(1);
    }

    @Test
    public void testUpdateEnrollmentPaymentStatus() {
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(false)
            .build();

        EnrollmentEntity savedEnrollment = enrollRepository.save(enrollment);
        entityManager.flush();

        // Update payment status
        savedEnrollment.setPaid(true);
        EnrollmentEntity updatedEnrollment = enrollRepository.save(savedEnrollment);
        entityManager.flush();

        assertThat(updatedEnrollment.isPaid()).isTrue();
        
        // Verify using query method
        Optional<Boolean> isPaidStatus = enrollRepository.findIsPaidByStudentIdAndModuleId(
            testStudent.getStudentId(), 
            testModule.getModuleId()
        );
        assertThat(isPaidStatus).isPresent();
        assertThat(isPaidStatus.get()).isTrue();
    }

    @Test
    public void testDeleteEnrollment() {
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(false)
            .build();

        EnrollmentEntity savedEnrollment = enrollRepository.save(enrollment);
        entityManager.flush();

        UUID enrollmentId = savedEnrollment.getEnrolmentId();
        
        enrollRepository.delete(savedEnrollment);
        entityManager.flush();

        Optional<EnrollmentEntity> deletedEnrollment = enrollRepository.findById(enrollmentId);
        assertThat(deletedEnrollment).isEmpty();

        // Verify enrollment no longer exists for student and module
        boolean exists = enrollRepository.existsByStudentStudentIdAndModuleModuleId(
            testStudent.getStudentId(), 
            testModule.getModuleId()
        );
        assertThat(exists).isFalse();
    }

    @Test
    public void testMultipleStudentsEnrolledInSameModule() {
        // Create second student
        User student2User = persistTestStudent("teststudent2@example.com", "Test Student 2");
        entityManager.flush();

        StudentEntity student2 = StudentEntity.builder()
            .user(student2User)
            .firstName("Test2")
            .lastName("Student2")
            .address("456 Test Ave")
            .city("Test City")
            .country("Test Country")
            .phoneNumber("2234567890")
            .bio("Test student 2 bio")
            .isActive(true)
            .build();
        student2 = studentProfileRepository.save(student2);
        entityManager.flush();

        // Enroll both students in same module
        EnrollmentEntity enrollment1 = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(true)
            .build();

        EnrollmentEntity enrollment2 = EnrollmentEntity.builder()
            .student(student2)
            .module(testModule)
            .isPaid(false)
            .build();

        enrollRepository.save(enrollment1);
        enrollRepository.save(enrollment2);
        entityManager.flush();

        // Verify both students are enrolled
        boolean student1Enrolled = enrollRepository.existsByStudentStudentIdAndModuleModuleId(
            testStudent.getStudentId(), testModule.getModuleId()
        );
        boolean student2Enrolled = enrollRepository.existsByStudentStudentIdAndModuleModuleId(
            student2.getStudentId(), testModule.getModuleId()
        );

        assertThat(student1Enrolled).isTrue();
        assertThat(student2Enrolled).isTrue();

        // Verify payment status for each
        Optional<Boolean> student1Paid = enrollRepository.findIsPaidByStudentIdAndModuleId(
            testStudent.getStudentId(), testModule.getModuleId()
        );
        Optional<Boolean> student2Paid = enrollRepository.findIsPaidByStudentIdAndModuleId(
            student2.getStudentId(), testModule.getModuleId()
        );

        assertThat(student1Paid).isPresent();
        assertThat(student1Paid.get()).isTrue();
        assertThat(student2Paid).isPresent();
        assertThat(student2Paid.get()).isFalse();
    }
}