package com.tutoring.Tutorverse.Repository;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.GenericJDBCException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nimbusds.openid.connect.sdk.claims.Gender;
import com.tutoring.Tutorverse.Model.*;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class ScheduleRepoTests extends BaseRepositoryTest {

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

    private ModuelsEntity testModule;
    private DomainEntity testDomain;
    private TutorEntity testTutor;
    private RecurrentEntity WeeklyRecurrent;
    private RecurrentEntity DailyRecurrent;
    private RecurrentEntity SpecificRecurrent;

    @BeforeEach
    public void setUp() {
        createStandardRoles();
        
        // Create test tutor user and profile
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
            .phoneNo("1234567890")
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
            .module(testModule)
            .isPaid(true)
            .build();
        enrollmentRepository.save(enrollment);
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
    private ScheduleEntity createSchedule(LocalDate date, LocalTime time, int duration, int weekNumber, RecurrentEntity recurrent) {
        return ScheduleEntity.builder()
            .module(testModule)
            .date(date)
            .time(time)
            .duration(duration)
            .weekNumber(weekNumber)
            .recurrent(recurrent)
            .build();
    }

    private ScheduleEntity createSpecificSchedule(LocalDate date, LocalTime time, int duration) {
        return createSchedule(date, time, duration, 0, SpecificRecurrent);
    }

    private ScheduleEntity createWeeklySchedule(LocalDate date, LocalTime time, int duration, int weekNumber) {
        return createSchedule(date, time, duration, weekNumber, WeeklyRecurrent);
    }

    private ScheduleEntity createDailySchedule(LocalDate date, LocalTime time, int duration) {
        return createSchedule(date, time, duration, 8, DailyRecurrent);
    }

    private ScheduleEntity saveAndFlush(ScheduleEntity schedule) {
        ScheduleEntity saved = scheduleRepository.save(schedule);
        entityManager.flush();
        return saved;
    }

    private void assertScheduleClash(ScheduleEntity newSchedule) {
        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testCreateSchedule() {
        ScheduleEntity schedule = createWeeklySchedule(
            LocalDate.now().plusDays(1), 
            LocalTime.of(10, 0), 
            60, 
            1
        );

        ScheduleEntity savedSchedule = saveAndFlush(schedule);

        assertThat(savedSchedule.getScheduleId()).isNotNull();
        assertThat(savedSchedule.getModule().getModuleId()).isEqualTo(testModule.getModuleId());
        assertThat(savedSchedule.getDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(savedSchedule.getTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(savedSchedule.getDuration()).isEqualTo(60);
        assertThat(savedSchedule.getCreatedAt()).isNotNull();
        assertThat(savedSchedule.getUpdatedAt()).isNotNull();
    }

    @Test
    public void testScheduleWithMissingMandatoryFields() {
        ScheduleEntity scheduleWithoutModule = ScheduleEntity.builder()
            .date(LocalDate.now().plusDays(1))
            .time(LocalTime.of(10, 0))
            .duration(60)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(scheduleWithoutModule);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test missing date
        ScheduleEntity scheduleWithoutDate = ScheduleEntity.builder()
            .module(testModule)
            .time(LocalTime.of(10, 0))
            .duration(60)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(scheduleWithoutDate);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);

        // Test missing time
        ScheduleEntity scheduleWithoutTime = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.now().plusDays(1))
            .duration(60)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(scheduleWithoutTime);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);

        // Test missing duration
        ScheduleEntity scheduleWithoutDuration = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.now().plusDays(1))
            .time(LocalTime.of(10, 0))
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(scheduleWithoutDuration);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testFindByModuleId() {
        ScheduleEntity schedule = createSpecificSchedule(
            LocalDate.now().plusDays(1), 
            LocalTime.of(10, 0), 
            60
        );

        saveAndFlush(schedule);

        List<ScheduleEntity> schedules = scheduleRepository.findByModule_ModuleId(testModule.getModuleId());
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getModule().getModuleId()).isEqualTo(testModule.getModuleId());
    }

    @Test
    public void testFindByTutorId() {
        ScheduleEntity schedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.now().plusDays(1))
            .time(LocalTime.of(10, 0))
            .duration(60)
            .build();

        scheduleRepository.save(schedule);
        entityManager.flush();

        List<ScheduleEntity> schedules = scheduleRepository.findByModule_TutorId(testTutor.getTutorId());
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getModule().getTutorId()).isEqualTo(testTutor.getTutorId());
    }

    @Test
    public void testFindByDate() {
        LocalDate testDate = LocalDate.now().plusDays(1);
        
        ScheduleEntity schedule = ScheduleEntity.builder()
            .module(testModule)
            .date(testDate)
            .time(LocalTime.of(10, 0))
            .duration(60)
            .build();

        scheduleRepository.save(schedule);
        entityManager.flush();

        List<ScheduleEntity> schedules = scheduleRepository.findByDate(testDate);
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getDate()).isEqualTo(testDate);
    }

    @Test
    public void testFindByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);
        
        ScheduleEntity schedule1 = ScheduleEntity.builder()
            .module(testModule)
            .date(startDate.minusDays(3))
            .time(LocalTime.of(10, 0))
            .duration(60)
            .build();

        ScheduleEntity schedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(startDate.plusDays(3))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        entityManager.flush();

        List<ScheduleEntity> schedules = scheduleRepository.findByDateBetween(startDate, endDate);
        assertThat(schedules).hasSize(1);
    }

    @Test
    public void testFindByWeekNumber() {
        ScheduleEntity schedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.now().plusDays(1))
            .time(LocalTime.of(10, 0))
            .duration(60)
            .weekNumber(1)
            .build();

        scheduleRepository.save(schedule);
        entityManager.flush();

        List<ScheduleEntity> schedules = scheduleRepository.findByWeekNumber(1);
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getWeekNumber()).isEqualTo(1);
    }

    @Test
    public void testFindByRecurrentType() {
        ScheduleEntity schedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.now().plusDays(1))
            .time(LocalTime.of(10, 0))
            .duration(60)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(schedule);
        entityManager.flush();

        List<ScheduleEntity> schedules = scheduleRepository.findByRecurrent_RecurrentType("WEEKLY");
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getRecurrent().getRecurrentType()).isEqualTo("WEEKLY");
    }

        @Test
    public void testScheduleUpdate() {
        ScheduleEntity schedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.now().plusDays(1))
            .time(LocalTime.of(10, 0))
            .duration(60)
            .build();

        ScheduleEntity savedSchedule = scheduleRepository.save(schedule);
        entityManager.flush();

        // Update the schedule
        savedSchedule.setTime(LocalTime.of(11, 0));
        savedSchedule.setDuration(90);
        
        ScheduleEntity updatedSchedule = scheduleRepository.save(savedSchedule);
        entityManager.flush();

        assertThat(updatedSchedule.getTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(updatedSchedule.getDuration()).isEqualTo(90);
        assertThat(updatedSchedule.getUpdatedAt()).isAfter(updatedSchedule.getCreatedAt());
    }

    @Test
    public void testDeleteSchedule() {
        ScheduleEntity schedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.now().plusDays(1))
            .time(LocalTime.of(10, 0))
            .duration(60)
            .build();

        ScheduleEntity savedSchedule = scheduleRepository.save(schedule);
        entityManager.flush();

        UUID scheduleId = savedSchedule.getScheduleId();
        
        scheduleRepository.delete(savedSchedule);
        entityManager.flush();

        Optional<ScheduleEntity> deletedSchedule = scheduleRepository.findById(scheduleId);
        assertThat(deletedSchedule).isEmpty();
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASH-SPECIFIC-SPECIFIC++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashSpecificDate_SpecificDateNormalCondition_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_SpecificDateNormalCondition_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(13, 1))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_SpecificDateNormalCondition_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 59))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashSpecificDate_SpecificDateBoundaryConditionNight_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_SpecificDateBoundaryConditionNight_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(00, 10))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_SpecificDateBoundaryConditionNight_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 30))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashSpecificDate_SpecificDateBoundaryConditionMorning_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule4 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(23, 17))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule4);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_SpecificDateBoundaryConditionMorning_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_SpecificDateBoundaryConditionMorning_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 01))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASH-SPECIFIC-WEEKLY+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashSpecificDate_WeeklyDateNormalCondition_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 06))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_WeeklyDateNormalCondition_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(13, 1))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_WeeklyDateNormalCondition_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 59))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashSpecificDate_WeeklyDateBoundaryConditionNight_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_WeeklyDateBoundaryConditionNight_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 10))
            .duration(60)
            .weekNumber(2)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_WeeklyDateBoundaryConditionNight_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 30))
            .duration(60)
            .weekNumber(1)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);

    }
    
    @Test
    public void testClashSpecificDate_WeeklyDateBoundaryConditionMorning_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule4 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 17))
            .duration(60)
            .weekNumber(7)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule4);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_WeeklyDateBoundaryConditionMorning_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(1)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_WeeklyDateBoundaryConditionMorning_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 01))
            .duration(60)
            .weekNumber(1)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASH-SPECIFIC-DAILY+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashSpecificDate_DailyDateBoundaryConditionMorning_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule4 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 17))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule4);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_DailyDateBoundaryConditionMorning_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_DailyDateBoundaryConditionMorning_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 01))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    @Test
    public void testClashSpecificDate_DailyDateNormalCondition_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 06))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_DailyDateNormalCondition_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(13, 1))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_DailyDateNormalCondition_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 59))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashSpecificDate_DailyDateBoundaryConditionNight_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_DailyDateBoundaryConditionNight_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 10))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashSpecificDate_DailyDateBoundaryConditionNight_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 30))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASH-WEEKLY-SPECIFIC++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashWeekly_SpecificDateNormalCondition_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10)) // from this date
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)   // every monday
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20)) //monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_SpecificDateNormalCondition_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(13, 1))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_SpecificDateNormalCondition_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20))
            .time(LocalTime.of(14, 59))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashWeekly_SpecificDateBoundaryConditionNight_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_SpecificDateBoundaryConditionNight_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(00, 10))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_SpecificDateBoundaryConditionNight_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 30))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashWeekly_SpecificDateBoundaryConditionMorning_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule4 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(23, 17))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule4);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_SpecificDateBoundaryConditionMorning_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_SpecificDateBoundaryConditionMorning_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 01))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASH-WEEKLY-WEEKLY+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashWeekly_WeeklyNormalCondition_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10)) // from this date
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)   // every monday
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20)) //monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_WeeklyNormalCondition_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(13, 1))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_WeeklyNormalCondition_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20))
            .time(LocalTime.of(14, 59))
            .duration(60)
            .weekNumber(1)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashWeekly_WeeklyBoundaryConditionNight_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_WeeklyBoundaryConditionNight_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(00, 10))
            .duration(60)
            .weekNumber(2)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_WeeklyBoundaryConditionNight_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 30))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashWeekly_WeeklyBoundaryConditionMorning_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule4 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(23, 17))
            .duration(60)
            .weekNumber(7)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule4);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_WeeklyBoundaryConditionMorning_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_WeeklyBoundaryConditionMorning_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 01))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASH-WEEKLY-DAILY++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashWeekly_DailyNormalCondition_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10)) // from this date
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)   // every monday
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20)) //monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_DailyDateNormalCondition_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(13, 1))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_DailyNormalCondition_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20))
            .time(LocalTime.of(14, 59))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashWeekly_DailyBoundaryConditionNight_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_DailyDateBoundaryConditionNight_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(00, 10))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_DailyBoundaryConditionNight_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 30))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashWeekly_DailyBoundaryConditionMorning_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule4 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(23, 17))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule4);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_DailyDateBoundaryConditionMorning_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashWeekly_DailyBoundaryConditionMorning_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 01))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASH-DAILY-SPECIFIC++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashDaily_SpecificDateNormalCondition_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10)) // from this date
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20)) //monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_SpecificDateNormalCondition_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(13, 1))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_SpecificDateNormalCondition_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20))
            .time(LocalTime.of(14, 59))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashDaily_SpecificDateBoundaryConditionNight_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_SpecificDateBoundaryConditionNight_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(00, 10))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_SpecificDateBoundaryConditionNight_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 30))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashDaily_SpecificDateBoundaryConditionMorning_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule4 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(23, 17))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule4);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_SpecificDateBoundaryConditionMorning_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_SpecificDateBoundaryConditionMorning_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 01))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASH-DAILY-WEEKLY+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashDaily_WeeklyNormalCondition_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10)) // from this date
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20)) //monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaikly_WeeklyNormalCondition_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(13, 1))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_WeeklyNormalCondition_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20))
            .time(LocalTime.of(14, 59))
            .duration(60)
            .weekNumber(1)
            .recurrent(SpecificRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashDaily_WeeklyBoundaryConditionNight_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_WeeklyBoundaryConditionNight_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(00, 10))
            .duration(60)
            .weekNumber(2)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_WeeklyBoundaryConditionNight_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 30))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashDaily_WeeklyBoundaryConditionMorning_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule4 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(23, 17))
            .duration(60)
            .weekNumber(7)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule4);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_WeeklyBoundaryConditionMorning_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_WeeklyBoundaryConditionMorning_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)   // every monday
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 01))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASH-DAILY-DAILY++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashDaily_DailyNormalCondition_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10)) // from this date
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)  
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20)) //monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_DailyDateNormalCondition_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)  
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(13, 1))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_DailyNormalCondition_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)  
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20))
            .time(LocalTime.of(14, 59))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashDaily_DailyBoundaryConditionNight_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)  
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_DailyDateBoundaryConditionNight_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)  
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(00, 10))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_DailyBoundaryConditionNight_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)  
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 30))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashDaily_DailyBoundaryConditionMorning_1() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)  
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule4 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(23, 17))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule4);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_DailyDateBoundaryConditionMorning_2() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)  
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule2);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    @Test
    public void testClashDaily_DailyBoundaryConditionMorning_3() {
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)  
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule3 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 01))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        assertThatThrownBy(() -> {
            scheduleRepository.save(newSchedule3);
            entityManager.flush();
        }).isInstanceOf(GenericJDBCException.class);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASHFREE-SPECIFIC-SPECIFIC++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashfreeSpecificDate_SpecificDateNormalCondition(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(15, 50))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule2 = scheduleRepository.save(newSchedule2);
        entityManager.flush();
        assertThat(savedNewSchedule2.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecificDate_SpecificDateNormalCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(12, 50))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecificDate_SpecificDateNormalCondition_3(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Different date - no clash possible
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecificDate_SpecificDateBoundaryCondition(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        ScheduleEntity newSchedule2 = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(22, 10))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule2 = scheduleRepository.save(newSchedule2);
        entityManager.flush();
        assertThat(savedNewSchedule2.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecificDate_SpecificDateBoundaryCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Next day - no clash possible for specific dates
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(00, 20))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecificDate_SpecificDateBoundaryCondition_3(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Previous day early time - no clash possible for specific dates
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(22, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASHFREE-SPECIFIC-WEEKLY++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashfreeSpecific_WeeklyNormalCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on different day of week - no clash
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14)) // Tuesday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(2) // Tuesday
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecific_WeeklyNormalCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on same day but different time - no clash
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(1) // Monday
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecific_WeeklyNormalCondition_3(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on different day with different time - no clash
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14)) // Tuesday
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(2) // Tuesday
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecific_WeeklyBoundaryCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on next day
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14)) // Tuesday
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(2) // Tuesday
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecific_WeeklyBoundaryCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(01, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on different day at safe time
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12)) // Sunday
            .time(LocalTime.of(22, 30))
            .duration(60)
            .weekNumber(7) // Sunday
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASHFREE-SPECIFIC-DAILY++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashfreeSpecific_DailyNormalCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily starting before specific date
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecific_DailyNormalCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with different time - no clash
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecific_DailyNormalCondition_3(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with different time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(12, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecific_DailyBoundaryCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with boundary time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(22, 10))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeSpecific_DailyBoundaryCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with different time - no clash
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14))
            .time(LocalTime.of(02, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASHFREE-WEEKLY-SPECIFIC++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashfreeWeekly_SpecificNormalCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific on different day
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14)) // Tuesday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_SpecificNormalCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific on same day but different time
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 20)) // Monday next week
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_SpecificNormalCondition_3(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific before weekly start date
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 06)) // Monday previous week
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_SpecificBoundaryCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific on Tuesday early morning
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14)) // Tuesday
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_SpecificBoundaryCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific on Sunday night before
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12)) // Sunday
            .time(LocalTime.of(23, 00))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASHFREE-WEEKLY-WEEKLY++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashfreeWeekly_WeeklyNormalCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on different day
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14)) // Tuesday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(2)
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_WeeklyNormalCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on same day but different time
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_WeeklyNormalCondition_3(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on same day, early time
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(12, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_WeeklyBoundaryCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on Tuesday
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 14)) // Tuesday
            .time(LocalTime.of(00, 30))
            .duration(60)
            .weekNumber(2)
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_WeeklyBoundaryCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly on Sunday
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12)) // Sunday
            .time(LocalTime.of(23, 00))
            .duration(60)
            .weekNumber(7)
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASHFREE-WEEKLY-DAILY++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashfreeWeekly_DailyNormalCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with different time
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_DailyNormalCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with early time
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(12, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_DailyBoundaryCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with boundary time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(22, 10))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeWeekly_DailyBoundaryCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with boundary time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(01, 30))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASHFREE-DAILY-SPECIFIC++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashfreeDaily_SpecificNormalCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific before daily start date
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 9))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_SpecificNormalCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific with different time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 15))
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_SpecificNormalCondition_3(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific with early time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(12, 0))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_SpecificBoundaryCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific with boundary time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(22, 10))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_SpecificBoundaryCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Specific at different time - no clash
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 9))
            .time(LocalTime.of(21, 30))
            .duration(60)
            .weekNumber(0)
            .recurrent(SpecificRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASHFREE-DAILY-WEEKLY++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashfreeDaily_WeeklyNormalCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly with different time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_WeeklyNormalCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly with early time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(12, 0))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_WeeklyBoundaryCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly with boundary time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(22, 10))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_WeeklyBoundaryCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Weekly with boundary time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 13)) // Monday
            .time(LocalTime.of(01, 30))
            .duration(60)
            .weekNumber(1)
            .recurrent(WeeklyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++CLASHFREE-DAILY-DAILY++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Test
    public void testClashfreeDaily_DailyNormalCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with different time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(16, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_DailyNormalCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with early time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(12, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_DailyNormalCondition_3(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(14, 0))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily starting after existing daily with different time
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 15))
            .time(LocalTime.of(16, 30))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_DailyBoundaryCondition_1(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(23, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with boundary time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(22, 10))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }

    @Test
    public void testClashfreeDaily_DailyBoundaryCondition_2(){
        ScheduleEntity existingSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 10))
            .time(LocalTime.of(00, 15))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        scheduleRepository.save(existingSchedule);
        entityManager.flush();

        // Daily with boundary time that doesn't overlap
        ScheduleEntity newSchedule = ScheduleEntity.builder()
            .module(testModule)
            .date(LocalDate.of(2025, 10, 12))
            .time(LocalTime.of(01, 30))
            .duration(60)
            .weekNumber(8)
            .recurrent(DailyRecurrent)
            .build();

        ScheduleEntity savedNewSchedule = scheduleRepository.save(newSchedule);
        entityManager.flush();
        assertThat(savedNewSchedule.getScheduleId()).isNotNull();
    }



    // @Test
    // public void testFindUpcomingSchedules() {
    //     LocalDate futureDate = LocalDate.now().plusDays(1);
        
    //     ScheduleEntity upcomingSchedule = ScheduleEntity.builder()
    //         .module(testModule)
    //         .date(futureDate)
    //         .time(LocalTime.of(10, 0))
    //         .duration(60)
    //         .build();

    //     ScheduleEntity pastSchedule = ScheduleEntity.builder()
    //         .module(testModule)
    //         .date(LocalDate.now().minusDays(1))
    //         .time(LocalTime.of(10, 0))
    //         .duration(60)
    //         .build();

    //     scheduleRepository.save(upcomingSchedule);
    //     scheduleRepository.save(pastSchedule);
    //     entityManager.flush();

    //     List<ScheduleEntity> upcomingSchedules = 
    //         scheduleRepository.findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate.now());
        
    //     assertThat(upcomingSchedules).hasSize(1);
    //     assertThat(upcomingSchedules.get(0).getDate()).isEqualTo(futureDate);
    // }

    // @Test
    // public void testFindMatchingScheduleFunction() {
    //     // This test assumes the find_matching_schedule function exists in the database
    //     // The function should return a schedule ID if a matching schedule is found
        
    //     ScheduleEntity schedule = ScheduleEntity.builder()
    //         .module(testModule)
    //         .date(LocalDate.now().plusDays(1))
    //         .time(LocalTime.of(10, 0))
    //         .duration(60)
    //         .build();

    //     ScheduleEntity savedSchedule = scheduleRepository.save(schedule);
    //     entityManager.flush();

    //     try {
    //         UUID matchingScheduleId = scheduleRepository.findMatchingSchedule(
    //             LocalDate.now().plusDays(1),
    //             LocalTime.of(10, 0),
    //             testModule.getModuleId()
    //         );
            
    //         // If the function exists and works, it should return the schedule ID
    //         if (matchingScheduleId != null) {
    //             assertThat(matchingScheduleId).isEqualTo(savedSchedule.getScheduleId());
    //         }
    //     } catch (Exception e) {
    //         // Function might not exist in test database, that's okay
    //         System.out.println("find_matching_schedule function not available in test database: " + e.getMessage());
    //     }
    // }

    // @Test
    // public void testGetUpcomingSchedulesFunction() {
    //     // This test assumes the get_upcoming_schedules function exists in the database
        
    //     ScheduleEntity schedule = ScheduleEntity.builder()
    //         .module(testModule)
    //         .date(LocalDate.now().plusDays(1))
    //         .time(LocalTime.of(10, 0))
    //         .duration(60)
    //         .build();

    //     scheduleRepository.save(schedule);
    //     entityManager.flush();

    //     try {
    //         List<Object[]> upcomingSchedules = scheduleRepository.getUpcomingSchedules(
    //             LocalDate.now(),
    //             LocalTime.of(9, 0),
    //             testModule.getModuleId(),
    //             testTutor.getTutorId(),
    //             10
    //         );
            
    //         // If the function exists and works, it should return results
    //         if (upcomingSchedules != null) {
    //             assertThat(upcomingSchedules).isNotNull();
    //         }
    //     } catch (Exception e) {
    //         // Function might not exist in test database, that's okay
    //         System.out.println("get_upcoming_schedules function not available in test database: " + e.getMessage());
    //     }
    // }

    // @Test
    // public void testGetUpcomingSchedulesForStudentFunction() {
    //     // This test assumes the get_upcoming_schedules_student function exists in the database
        
    //     ScheduleEntity schedule = ScheduleEntity.builder()
    //         .module(testModule)
    //         .date(LocalDate.now().plusDays(1))
    //         .time(LocalTime.of(10, 0))
    //         .duration(60)
    //         .build();

    //     scheduleRepository.save(schedule);
    //     entityManager.flush();

    //     // Create test student
    //     UUID studentId = persistTestStudent("teststudent@example.com", "Test Student").getId();

    //     try {
    //         List<Object[]> upcomingSchedules = scheduleRepository.getUpcomingSchedulesForStudent(
    //             LocalDate.now(),
    //             LocalTime.of(9, 0),
    //             testModule.getModuleId(),
    //             studentId,
    //             10
    //         );
            
    //         // If the function exists and works, it should return results
    //         if (upcomingSchedules != null) {
    //             assertThat(upcomingSchedules).isNotNull();
    //         }
    //     } catch (Exception e) {
    //         // Function might not exist in test database, that's okay
    //         System.out.println("get_upcoming_schedules_student function not available in test database: " + e.getMessage());
    //     }
    // }


}