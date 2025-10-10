package com.tutoring.Tutorverse.Repository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import com.tutoring.Tutorverse.Model.DomainEntity;
import com.tutoring.Tutorverse.Model.EnrollmentEntity;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import com.tutoring.Tutorverse.Model.RecurrentEntity;
import com.tutoring.Tutorverse.Model.ScheduleEntity;
import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Model.TutorEntity;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.transaction.Transactional;




@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class Schedule2RepoTests extends BaseRepositoryTest {
    
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ModulesRepository modulesRepository;

    @Autowired
    private EnrollRepository enrollmentRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private RecurrentRepository recurrentRepository;

    @Autowired
    private TutorProfileRepository tutorProfileRepository;

    private ModuelsEntity testModule1;
    private ModuelsEntity testModule2;
    private DomainEntity testDomain;
    private TutorEntity testTutor1;
    private TutorEntity testTutor2;
    private RecurrentEntity WeeklyRecurrent;
    private RecurrentEntity DailyRecurrent;
    private RecurrentEntity SpecificRecurrent;

    @BeforeEach
    public void setUp() {
        createStandardRoles();
        
        // Create test tutor user and profile
        User tutorUser = persistTestTutor("testtutor@example.com", "Test Tutor");
        entityManager.flush();
        User tutorUser2 = persistTestTutor("testtutor2@example.com", "Test Tutor2");
        entityManager.flush();

        testTutor1 = TutorEntity.builder()
            .user(tutorUser)
            .firstName("Test")
            .lastName("Tutor")
            .address("123 Test St")
            .city("Test City")
            .country("Test Country")
            .gender(TutorEntity.Gender.MALE)
            .phoneNo("1234567890")
            .bio("Test tutor bio")
            .build();
        testTutor1 = tutorProfileRepository.save(testTutor1);
        entityManager.flush();

        testTutor2 = TutorEntity.builder()
            .user(tutorUser2)
            .firstName("Test2")
            .lastName("Tutor2")
            .address("124 Test St")
            .city("Test City2")
            .country("Test Country2")
            .gender(TutorEntity.Gender.MALE)
            .phoneNo("1234267890")
            .bio("Test tutor bio")
            .build();
        testTutor2 = tutorProfileRepository.save(testTutor2);
        entityManager.flush();

        // Create test domain
        testDomain = DomainEntity.builder()
            .name("Mathematics")
            .build();
        testDomain = domainRepository.save(testDomain);
        entityManager.flush();

        // Create test module
        testModule1 = ModuelsEntity.builder()
            .tutorId(testTutor1.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .duration(Duration.ofHours(1))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();
        testModule1 = modulesRepository.save(testModule1);
        entityManager.flush();

        testModule2 = ModuelsEntity.builder()
            .tutorId(testTutor2.getTutorId())
            .name("Calculus II")
            .domain(testDomain)
            .fee(new BigDecimal("70.00"))
            .duration(Duration.ofHours(1))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();
        testModule2 = modulesRepository.save(testModule2);
        entityManager.flush();

        User studentUser = persistTestStudent("teststudent@example.com", "Test Student");
        entityManager.flush();

        StudentEntity testStudent = StudentEntity.builder()
            .user(studentUser)
            .firstName("Test")
            .lastName("Student")
            .address("456 Student Ave")
            .city("Student City")
            .country("Student Country")
            .phoneNumber("0987654321")
            .bio("Test student bio")
            .build();
        testStudent = studentProfileRepository.save(testStudent);
        entityManager.flush();

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule1)
            .isPaid(true)
            .build();
        enrollmentRepository.save(enrollment);
        entityManager.flush();

        EnrollmentEntity enrollment2 = EnrollmentEntity.builder()
            .student(testStudent)
            .module(testModule2)
            .isPaid(true)
            .build();
        enrollmentRepository.save(enrollment2);
        entityManager.flush();

        // Create test recurrent
        WeeklyRecurrent = RecurrentEntity.builder()
            .recurrentType("WEEKLY")
            .build();
        WeeklyRecurrent = recurrentRepository.save(WeeklyRecurrent);
        entityManager.flush();
        DailyRecurrent = RecurrentEntity.builder()
            .recurrentType("DAILY")
            .build();
        DailyRecurrent = recurrentRepository.save(DailyRecurrent);
        entityManager.flush();
        SpecificRecurrent = RecurrentEntity.builder()
            .recurrentType("SPECIFIC")
            .build();
        SpecificRecurrent = recurrentRepository.save(SpecificRecurrent);
        entityManager.flush();
    }

    // Helper methods for creating schedules
    private ScheduleEntity createSchedule(ModuelsEntity module,LocalDate date, LocalTime time, int duration, int weekNumber, RecurrentEntity recurrent) {
        return ScheduleEntity.builder()
            .module(module)
            .date(date)
            .time(time)
            .duration(duration)
            .weekNumber(weekNumber)
            .recurrent(recurrent)
            .build();
    }

    private ScheduleEntity createSpecificSchedule(ModuelsEntity module,LocalDate date, LocalTime time, int duration) {
        return createSchedule(module, date, time, duration, 0, SpecificRecurrent);
    }

    private ScheduleEntity createWeeklySchedule(ModuelsEntity module,LocalDate date, LocalTime time, int duration, int weekNumber) {
        return createSchedule(module,date, time, duration, weekNumber, WeeklyRecurrent);
    }

    private ScheduleEntity createDailySchedule(ModuelsEntity module,LocalDate date, LocalTime time, int duration) {
        return createSchedule(module, date, time, duration, 8, DailyRecurrent);
    }

    private ScheduleEntity saveAndFlush(ScheduleEntity schedule) {
        ScheduleEntity saved = scheduleRepository.save(schedule);
        entityManager.flush();
        return saved;
    }

    @Test
    public void testInitialValuesAreCreated() {
        // Test that all entities created in setUp() are properly initialized
        
        // Test tutors are created
        assertThat(testTutor1).isNotNull();
        assertThat(testTutor1.getTutorId()).isNotNull();
        assertThat(testTutor1.getFirstName()).isEqualTo("Test");
        assertThat(testTutor1.getLastName()).isEqualTo("Tutor");
        assertThat(testTutor1.getUser()).isNotNull();
        assertThat(testTutor1.getUser().getEmail()).isEqualTo("testtutor@example.com");
        
        assertThat(testTutor2).isNotNull();
        assertThat(testTutor2.getTutorId()).isNotNull();
        assertThat(testTutor2.getFirstName()).isEqualTo("Test2");
        assertThat(testTutor2.getLastName()).isEqualTo("Tutor2");
        assertThat(testTutor2.getUser()).isNotNull();
        assertThat(testTutor2.getUser().getEmail()).isEqualTo("testtutor2@example.com");
        
        // Test domain is created
        assertThat(testDomain).isNotNull();
        assertThat(testDomain.getDomainId()).isNotNull();
        assertThat(testDomain.getName()).isEqualTo("Mathematics");
        
        // Test modules are created
        assertThat(testModule1).isNotNull();
        assertThat(testModule1.getModuleId()).isNotNull();
        assertThat(testModule1.getName()).isEqualTo("Calculus I");
        assertThat(testModule1.getTutorId()).isEqualTo(testTutor1.getTutorId());
        assertThat(testModule1.getDomain()).isEqualTo(testDomain);
        assertThat(testModule1.getFee()).isEqualTo(new BigDecimal("50.00"));
        assertThat(testModule1.getStatus()).isEqualTo(ModuelsEntity.ModuleStatus.Active);
        
        assertThat(testModule2).isNotNull();
        assertThat(testModule2.getModuleId()).isNotNull();
        assertThat(testModule2.getName()).isEqualTo("Calculus II");
        assertThat(testModule2.getTutorId()).isEqualTo(testTutor2.getTutorId());
        assertThat(testModule2.getDomain()).isEqualTo(testDomain);
        assertThat(testModule2.getFee()).isEqualTo(new BigDecimal("70.00"));
        assertThat(testModule2.getStatus()).isEqualTo(ModuelsEntity.ModuleStatus.Active);
        
        // Test recurrent entities are created
        assertThat(WeeklyRecurrent).isNotNull();
        assertThat(WeeklyRecurrent.getRecurrentId()).isNotNull();
        assertThat(WeeklyRecurrent.getRecurrentType()).isEqualTo("WEEKLY");
        
        assertThat(DailyRecurrent).isNotNull();
        assertThat(DailyRecurrent.getRecurrentId()).isNotNull();
        assertThat(DailyRecurrent.getRecurrentType()).isEqualTo("DAILY");
        
        assertThat(SpecificRecurrent).isNotNull();
        assertThat(SpecificRecurrent.getRecurrentId()).isNotNull();
        assertThat(SpecificRecurrent.getRecurrentType()).isEqualTo("SPECIFIC");
        
        // Test that student and enrollments are created by checking database state
        // Since we don't have direct references to these, we'll verify through repositories
        assertThat(studentProfileRepository.count()).isEqualTo(1);
        assertThat(enrollmentRepository.count()).isEqualTo(2);
        
        // Verify the enrollments exist for both modules by checking the count
        assertThat(enrollmentRepository.findAll()).hasSize(2);
        
        // Test that repositories are properly injected and working
        assertThat(scheduleRepository).isNotNull();
        assertThat(studentProfileRepository).isNotNull();
        assertThat(modulesRepository).isNotNull();
        assertThat(enrollmentRepository).isNotNull();
        assertThat(domainRepository).isNotNull();
        assertThat(recurrentRepository).isNotNull();
        assertThat(tutorProfileRepository).isNotNull();
        
        // Test helper methods work correctly
        ScheduleEntity testSchedule = createSpecificSchedule(testModule1, LocalDate.of(2025, 10, 15), LocalTime.of(10, 0), 60);
        assertThat(testSchedule).isNotNull();
        assertThat(testSchedule.getModule()).isEqualTo(testModule1);
        assertThat(testSchedule.getDate()).isEqualTo(LocalDate.of(2025, 10, 15));
        assertThat(testSchedule.getTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(testSchedule.getDuration()).isEqualTo(60);
        assertThat(testSchedule.getWeekNumber()).isEqualTo(0);
        assertThat(testSchedule.getRecurrent()).isEqualTo(SpecificRecurrent);
        
        ScheduleEntity weeklySchedule = createWeeklySchedule(testModule2, LocalDate.of(2025, 10, 20), LocalTime.of(14, 0), 90, 1);
        assertThat(weeklySchedule).isNotNull();
        assertThat(weeklySchedule.getModule()).isEqualTo(testModule2);
        assertThat(weeklySchedule.getWeekNumber()).isEqualTo(1);
        assertThat(weeklySchedule.getRecurrent()).isEqualTo(WeeklyRecurrent);
        
        ScheduleEntity dailySchedule = createDailySchedule(testModule1, LocalDate.of(2025, 10, 25), LocalTime.of(16, 30), 45);
        assertThat(dailySchedule).isNotNull();
        assertThat(dailySchedule.getModule()).isEqualTo(testModule1);
        assertThat(dailySchedule.getWeekNumber()).isEqualTo(8);
        assertThat(dailySchedule.getRecurrent()).isEqualTo(DailyRecurrent);
    }

    @Test
    public void testRepositoryOperations() {
        // Test basic repository operations work
        
        // Test saving a schedule
        ScheduleEntity schedule = createSpecificSchedule(testModule1, LocalDate.of(2025, 10, 20), LocalTime.of(9, 0), 60);
        ScheduleEntity saved = saveAndFlush(schedule);
        
        assertThat(saved.getScheduleId()).isNotNull();
        assertThat(scheduleRepository.findById(saved.getScheduleId())).isPresent();
        
        // Test finding schedules
        assertThat(scheduleRepository.findAll()).hasSize(1);
        
        // Test deleting a schedule
        scheduleRepository.delete(saved);
        entityManager.flush();
        assertThat(scheduleRepository.findAll()).hasSize(0);
    }

    @Test
    public void testDatabaseConstraints() {
        ScheduleEntity schedule1 = saveAndFlush(createSpecificSchedule(testModule1, LocalDate.of(2025, 10, 20), LocalTime.of(9, 0), 60));
        ScheduleEntity schedule2 = saveAndFlush(createSpecificSchedule(testModule2, LocalDate.of(2025, 10, 21), LocalTime.of(10, 0), 90));
        assertThat(schedule1.getScheduleId()).isNotEqualTo(schedule2.getScheduleId());
        assertThat(scheduleRepository.count()).isEqualTo(2);
        assertThat(schedule1.getModule().getModuleId()).isEqualTo(testModule1.getModuleId());
        assertThat(schedule2.getModule().getModuleId()).isEqualTo(testModule2.getModuleId());
    }

    // ===================================================================================ACTUAL TESTING==================================================================

    
}
