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
import java.util.Collection;
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
public class ModulesRepoTests extends BaseRepositoryTest {

    @Autowired
    private ModulesRepository modulesRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private TutorProfileRepository tutorProfileRepository;

    private DomainEntity testDomain;
    private TutorEntity testTutor;
    private User tutorUser;

    @BeforeEach
    public void setUp() {
        createStandardRoles();
        
        // Create test tutor user and profile
        tutorUser = persistTestTutor("testtutor@example.com", "Test Tutor");
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
    }

    @Test
    public void testCreateModule() {
        ModuelsEntity module = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .duration(Duration.ofHours(1))
            .status(ModuelsEntity.ModuleStatus.Active)
            .averageRatings(new BigDecimal("4.5"))
            .build();

        ModuelsEntity savedModule = modulesRepository.save(module);
        entityManager.flush();

        assertThat(savedModule.getModuleId()).isNotNull();
        assertThat(savedModule.getTutorId()).isEqualTo(testTutor.getTutorId());
        assertThat(savedModule.getName()).isEqualTo("Calculus I");
        assertThat(savedModule.getDomain().getName()).isEqualTo("Mathematics");
        assertThat(savedModule.getFee()).isEqualTo(new BigDecimal("50.00"));
        assertThat(savedModule.getDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(savedModule.getStatus()).isEqualTo(ModuelsEntity.ModuleStatus.Active);
        assertThat(savedModule.getCreatedAt()).isNotNull();
        assertThat(savedModule.getUpdatedAt()).isNotNull();
    }

    @Test
    public void testModuleWithMissingMandatoryFields() {
        // Test missing tutorId
        ModuelsEntity moduleWithoutTutorId = ModuelsEntity.builder()
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .build();

        assertThatThrownBy(() -> {
            modulesRepository.save(moduleWithoutTutorId);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);

        // Test missing name
        ModuelsEntity moduleWithoutName = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .build();

        assertThatThrownBy(() -> {
            modulesRepository.save(moduleWithoutName);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);

        // Test missing fee
        ModuelsEntity moduleWithoutFee = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .build();

        assertThatThrownBy(() -> {
            modulesRepository.save(moduleWithoutFee);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        ModuelsEntity module1 = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();

        ModuelsEntity module2 = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Advanced Calculus")
            .domain(testDomain)
            .fee(new BigDecimal("75.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();

        modulesRepository.save(module1);
        modulesRepository.save(module2);
        entityManager.flush();

        Collection<ModuelsEntity> foundModules = modulesRepository.findByNameContainingIgnoreCase("calculus");
        assertThat(foundModules).hasSize(2);
        assertThat(foundModules).extracting(ModuelsEntity::getName)
            .containsExactlyInAnyOrder("Calculus I", "Advanced Calculus");

        Collection<ModuelsEntity> foundModules2 = modulesRepository.findByNameContainingIgnoreCase("ADVANCED");
        assertThat(foundModules2).hasSize(1);
        assertThat(foundModules2.iterator().next().getName()).isEqualTo("Advanced Calculus");
    }

    @Test
    public void testFindByDomainId() {
        // Create another domain
        DomainEntity physicsDomain = DomainEntity.builder()
            .name("Physics")
            .build();
        physicsDomain = domainRepository.save(physicsDomain);
        entityManager.flush();

        ModuelsEntity mathModule = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();

        ModuelsEntity physicsModule = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Mechanics")
            .domain(physicsDomain)
            .fee(new BigDecimal("60.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();

        modulesRepository.save(mathModule);
        modulesRepository.save(physicsModule);
        entityManager.flush();

        Collection<ModuelsEntity> mathModules = modulesRepository.findByDomain_DomainId(testDomain.getDomainId());
        assertThat(mathModules).hasSize(1);
        assertThat(mathModules.iterator().next().getName()).isEqualTo("Calculus I");

        Collection<ModuelsEntity> physicsModules = modulesRepository.findByDomain_DomainId(physicsDomain.getDomainId());
        assertThat(physicsModules).hasSize(1);
        assertThat(physicsModules.iterator().next().getName()).isEqualTo("Mechanics");
    }

    @Test
    public void testExistsByTutorIdAndModuleId() {
        ModuelsEntity module = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();

        ModuelsEntity savedModule = modulesRepository.save(module);
        entityManager.flush();

        Boolean exists = modulesRepository.existsByTutorIdAndModuleId(
            testTutor.getTutorId(), 
            savedModule.getModuleId()
        );
        assertThat(exists).isTrue();

        Boolean notExists = modulesRepository.existsByTutorIdAndModuleId(
            UUID.randomUUID(), 
            savedModule.getModuleId()
        );
        assertThat(notExists).isFalse();
    }

    @Test
    public void testFindByTutorId() {
        ModuelsEntity module1 = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();

        ModuelsEntity module2 = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Algebra I")
            .domain(testDomain)
            .fee(new BigDecimal("45.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();

        modulesRepository.save(module1);
        modulesRepository.save(module2);
        entityManager.flush();

        Collection<ModuelsEntity> tutorModules = modulesRepository.findByTutor_TutorId(testTutor.getTutorId());
        assertThat(tutorModules).hasSize(2);
        assertThat(tutorModules).extracting(ModuelsEntity::getName)
            .containsExactlyInAnyOrder("Calculus I", "Algebra I");
    }

    @Test
    public void testModuleStatusValues() {
        // Test Draft status
        ModuelsEntity draftModule = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Draft Module")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .status(ModuelsEntity.ModuleStatus.Draft)
            .build();

        ModuelsEntity savedDraftModule = modulesRepository.save(draftModule);
        entityManager.flush();
        assertThat(savedDraftModule.getStatus()).isEqualTo(ModuelsEntity.ModuleStatus.Draft);

        // Test Archived status
        ModuelsEntity archivedModule = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Archived Module")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .status(ModuelsEntity.ModuleStatus.Archived)
            .build();

        ModuelsEntity savedArchivedModule = modulesRepository.save(archivedModule);
        entityManager.flush();
        assertThat(savedArchivedModule.getStatus()).isEqualTo(ModuelsEntity.ModuleStatus.Archived);
    }

    @Test
    public void testModuleUpdate() {
        ModuelsEntity module = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .status(ModuelsEntity.ModuleStatus.Draft)
            .averageRatings(new BigDecimal("0.0"))
            .build();

        ModuelsEntity savedModule = modulesRepository.save(module);
        entityManager.flush();

        // Update the module
        savedModule.setName("Advanced Calculus I");
        savedModule.setFee(new BigDecimal("75.00"));
        savedModule.setStatus(ModuelsEntity.ModuleStatus.Active);
        savedModule.setAverageRatings(new BigDecimal("4.2"));

        ModuelsEntity updatedModule = modulesRepository.save(savedModule);
        entityManager.flush();

        assertThat(updatedModule.getName()).isEqualTo("Advanced Calculus I");
        assertThat(updatedModule.getFee()).isEqualTo(new BigDecimal("75.00"));
        assertThat(updatedModule.getStatus()).isEqualTo(ModuelsEntity.ModuleStatus.Active);
        assertThat(updatedModule.getAverageRatings()).isEqualTo(new BigDecimal("4.2"));
        assertThat(updatedModule.getUpdatedAt()).isAfter(updatedModule.getCreatedAt());
    }

    @Test
    public void testDeleteModule() {
        ModuelsEntity module = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();

        ModuelsEntity savedModule = modulesRepository.save(module);
        entityManager.flush();

        UUID moduleId = savedModule.getModuleId();
        
        modulesRepository.delete(savedModule);
        entityManager.flush();

        Optional<ModuelsEntity> deletedModule = modulesRepository.findById(moduleId);
        assertThat(deletedModule).isEmpty();
    }

    @Test
    public void testDefaultAverageRatings() {
        ModuelsEntity module = ModuelsEntity.builder()
            .tutorId(testTutor.getTutorId())
            .name("Calculus I")
            .domain(testDomain)
            .fee(new BigDecimal("50.00"))
            .status(ModuelsEntity.ModuleStatus.Active)
            .build();

        ModuelsEntity savedModule = modulesRepository.save(module);
        entityManager.flush();

        // Should have default average ratings of 0.0
        assertThat(savedModule.getAverageRatings()).isEqualTo(new BigDecimal("0.0"));
    }
}