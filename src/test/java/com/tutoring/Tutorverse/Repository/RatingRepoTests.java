package com.tutoring.Tutorverse.Repository;

import org.hibernate.SessionFactory;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tutoring.Tutorverse.Model.*;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class RatingRepoTests extends BaseRepositoryTest {

    @Autowired
    private RatingRepository ratingRepository;

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
    private EnrollmentEntity testEnrollment;
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

        // Create test enrollment
        testEnrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule)
            .isPaid(true)
            .build();
        testEnrollment = enrollRepository.save(testEnrollment);
        entityManager.flush();
    }

    @Test
    public void testCreateRating() {
        RatingEntity rating = RatingEntity.builder()
            .enrollment(testEnrollment)  // Only set the enrollment, @MapsId will handle the ID
            .rating(new BigDecimal("4.5"))
            .feedback("Great module! Very helpful instructor.")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        RatingEntity savedRating = ratingRepository.save(rating);
        entityManager.flush();

        assertThat(savedRating.getEnrolmentId()).isEqualTo(testEnrollment.getEnrolmentId());
        assertThat(savedRating.getRating()).isEqualTo(new BigDecimal("4.5"));
        assertThat(savedRating.getFeedback()).isEqualTo("Great module! Very helpful instructor.");
        assertThat(savedRating.getStudentName()).isEqualTo("Test Student");
        assertThat(savedRating.getModuleId()).isEqualTo(testModule.getModuleId());
        assertThat(savedRating.getCreatedAt()).isNotNull();
    }

    @Test
    public void testRatingWithMissingMandatoryFields() {
        // Test missing moduleId
        RatingEntity ratingWithoutModuleId = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("4.5"))
            .feedback("Great module!")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .build();

        assertThatThrownBy(() -> {
            ratingRepository.save(ratingWithoutModuleId);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test missing createdAt
        RatingEntity ratingWithoutCreatedAt = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("4.5"))
            .feedback("Great module!")
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        assertThatThrownBy(() -> {
            ratingRepository.save(ratingWithoutCreatedAt);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testFindByEnrolmentId() {
        RatingEntity rating = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("4.0"))
            .feedback("Good module")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        ratingRepository.save(rating);
        entityManager.flush();

        Optional<RatingEntity> foundRating = ratingRepository.findByEnrolmentId(testEnrollment.getEnrolmentId());
        
        assertThat(foundRating).isPresent();
        assertThat(foundRating.get().getRating()).isEqualTo(new BigDecimal("4.0"));
        assertThat(foundRating.get().getFeedback()).isEqualTo("Good module");
    }

    @Test
    public void testExistsByEnrolmentId() {
        RatingEntity rating = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("3.5"))
            .feedback("Average module")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        ratingRepository.save(rating);
        entityManager.flush();

        boolean exists = ratingRepository.existsByEnrolmentId(testEnrollment.getEnrolmentId());
        assertThat(exists).isTrue();

        boolean notExists = ratingRepository.existsByEnrolmentId(UUID.randomUUID());
        assertThat(notExists).isFalse();
    }

    @Test
    public void testFindByModuleId() {
        // Create second enrollment for same module
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

        EnrollmentEntity enrollment2 = EnrollmentEntity.builder()
            .student(student2)
            .module(testModule)
            .isPaid(true)
            .build();
        enrollment2 = enrollRepository.save(enrollment2);
        entityManager.flush();

        // Create ratings for both enrollments
        RatingEntity rating1 = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("4.5"))
            .feedback("Excellent module!")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        RatingEntity rating2 = RatingEntity.builder()
            .enrollment(enrollment2)
            .rating(new BigDecimal("3.0"))
            .feedback("Ok module")
            .createdAt(Instant.now())
            .studentName("Test Student 2")
            .moduleId(testModule.getModuleId())
            .build();

        ratingRepository.save(rating1);
        ratingRepository.save(rating2);
        entityManager.flush();

        List<RatingEntity> ratings = ratingRepository.findByModuleId(testModule.getModuleId());
        
        assertThat(ratings).hasSize(2);
        assertThat(ratings).extracting(RatingEntity::getRating)
            .containsExactlyInAnyOrder(new BigDecimal("4.5"), new BigDecimal("3.0"));
    }

    @Test
    public void testFindAllByModuleId() {
        RatingEntity rating = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("5.0"))
            .feedback("Perfect module!")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        ratingRepository.save(rating);
        entityManager.flush();

        List<RatingEntity> ratings = ratingRepository.findAllByModuleId(testModule.getModuleId());
        
        assertThat(ratings).hasSize(1);
        assertThat(ratings.get(0).getRating()).isEqualTo(new BigDecimal("5.0"));
        assertThat(ratings.get(0).getFeedback()).isEqualTo("Perfect module!");
    }

    @Test
    public void testFindByStudentId() {
        // Create another module for the same student
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
        enrollment2 = enrollRepository.save(enrollment2);
        entityManager.flush();

        // Create ratings for both enrollments
        RatingEntity rating1 = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("4.0"))
            .feedback("Good calculus module")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        RatingEntity rating2 = RatingEntity.builder()
            .enrollment(enrollment2)
            .rating(new BigDecimal("3.5"))
            .feedback("Good algebra module")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(module2.getModuleId())
            .build();

        ratingRepository.save(rating1);
        ratingRepository.save(rating2);
        entityManager.flush();

        List<RatingEntity> studentRatings = ratingRepository.findByStudentId(testStudent.getStudentId());
        
        assertThat(studentRatings).hasSize(2);
        assertThat(studentRatings).extracting(RatingEntity::getFeedback)
            .containsExactlyInAnyOrder("Good calculus module", "Good algebra module");
    }

    @Test
    public void testRatingScalePrecision() {
        // Test various rating values to ensure precision/scale works correctly
        RatingEntity rating1 = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("1.0"))
            .feedback("Poor")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        RatingEntity savedRating1 = ratingRepository.save(rating1);
        entityManager.flush();

        assertThat(savedRating1.getRating()).isEqualTo(new BigDecimal("1.0"));

        // Update to test different values
        savedRating1.setRating(new BigDecimal("5.0"));
        RatingEntity updatedRating = ratingRepository.save(savedRating1);
        entityManager.flush();

        assertThat(updatedRating.getRating()).isEqualTo(new BigDecimal("5.0"));
    }

    @Test
    public void testDuplicateRatingForSameEnrollment() {
        RatingEntity rating1 = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("4.0"))
            .feedback("First rating")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        ratingRepository.save(rating1);
        entityManager.flush();

        // Try to create another rating for the same enrollment
        RatingEntity rating2 = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("5.0"))
            .feedback("Second rating")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        // This should fail due to primary key constraint
        assertThatThrownBy(() -> {
            ratingRepository.save(rating2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testUpdateRating() {
        RatingEntity rating = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("3.0"))
            .feedback("Initial rating")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        RatingEntity savedRating = ratingRepository.save(rating);
        entityManager.flush();

        // Update the rating
        savedRating.setRating(new BigDecimal("4.5"));
        savedRating.setFeedback("Updated rating - much better!");

        RatingEntity updatedRating = ratingRepository.save(savedRating);
        entityManager.flush();

        assertThat(updatedRating.getRating()).isEqualTo(new BigDecimal("4.5"));
        assertThat(updatedRating.getFeedback()).isEqualTo("Updated rating - much better!");
    }

    @Test
    public void testDeleteRating() {
        RatingEntity rating = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("4.0"))
            .feedback("Good module")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        ratingRepository.save(rating);
        entityManager.flush();

        UUID enrolmentId = testEnrollment.getEnrolmentId();
        
        ratingRepository.delete(rating);
        entityManager.flush();

        Optional<RatingEntity> deletedRating = ratingRepository.findByEnrolmentId(enrolmentId);
        assertThat(deletedRating).isEmpty();

        boolean exists = ratingRepository.existsByEnrolmentId(enrolmentId);
        assertThat(exists).isFalse();
    }

    @Test
    public void testRatingWithNullFeedback() {
        // Test that feedback can be null
        RatingEntity rating = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("4.0"))
            .feedback(null) // Null feedback should be allowed
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        RatingEntity savedRating = ratingRepository.save(rating);
        entityManager.flush();

        assertThat(savedRating.getFeedback()).isNull();
        assertThat(savedRating.getRating()).isEqualTo(new BigDecimal("4.0"));
    }


    @Test
    public void testAverageRatingCalculation() {
        // Create multiple ratings for the same module
        User student2User = persistTestStudent("teststudent2@example.com", "Test Student 2");
        entityManager.flush();

        StudentEntity testStudent2 = StudentEntity.builder()
            .user(student2User)
            .firstName("Test")
            .lastName("Student 2")
            .address("123 Test St")
            .city("Test City")
            .country("Test Country")
            .phoneNumber("1234567892")
            .bio("Test student 2 bio")
            .isActive(true)
            .build();
        testStudent2 = studentProfileRepository.save(testStudent2);
        entityManager.flush();

        // Create another enrollment for the second student
        EnrollmentEntity testEnrollment2 = EnrollmentEntity.builder()
            .student(testStudent2)
            .module(testModule)
            .isPaid(true)
            .build();
        testEnrollment2 = enrollRepository.save(testEnrollment2);
        entityManager.flush();

        // Create ratings for both students
        RatingEntity rating1 = RatingEntity.builder()
            .enrollment(testEnrollment)
            .rating(new BigDecimal("4.0"))
            .feedback("Good module")
            .createdAt(Instant.now())
            .studentName("Test Student")
            .moduleId(testModule.getModuleId())
            .build();

        RatingEntity rating2 = RatingEntity.builder()
            .enrollment(testEnrollment2)
            .rating(new BigDecimal("5.0"))
            .feedback("Excellent module")
            .createdAt(Instant.now())
            .studentName("Test Student 2")
            .moduleId(testModule.getModuleId())
            .build();

        ratingRepository.save(rating1);
        ratingRepository.save(rating2);
        entityManager.flush();


        // Calculate average rating
        Optional<ModuelsEntity> modules = modulesRepository.findByModuleId(testModule.getModuleId());
        assertThat(modules).isPresent();

        entityManager.refresh(modules.get());
        BigDecimal averageRating = modules.get().getAverageRatings();
        assertThat(averageRating).isEqualTo(new BigDecimal("4.5"));
    }
}