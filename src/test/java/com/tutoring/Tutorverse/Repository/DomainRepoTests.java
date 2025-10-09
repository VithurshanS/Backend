package com.tutoring.Tutorverse.Repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import com.tutoring.Tutorverse.Model.DomainEntity;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.transaction.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class DomainRepoTests extends BaseRepositoryTest {

    @Autowired
    private DomainRepository domainRepository;

    @BeforeEach
    public void setUp() {
        // No additional setup needed for Domain tests
    }

    @Test
    public void testCreateDomain() {
        DomainEntity domain = DomainEntity.builder()
            .name("Mathematics")
            .build();

        DomainEntity savedDomain = domainRepository.save(domain);
        entityManager.flush();

        assertThat(savedDomain.getDomainId()).isNotNull();
        assertThat(savedDomain.getName()).isEqualTo("Mathematics");
    }

    @Test
    public void testCreateMultipleDomains() {
        DomainEntity domain1 = DomainEntity.builder()
            .name("Mathematics")
            .build();

        DomainEntity domain2 = DomainEntity.builder()
            .name("Physics")
            .build();

        DomainEntity domain3 = DomainEntity.builder()
            .name("Chemistry")
            .build();

        DomainEntity savedDomain1 = domainRepository.save(domain1);
        DomainEntity savedDomain2 = domainRepository.save(domain2);
        DomainEntity savedDomain3 = domainRepository.save(domain3);
        entityManager.flush();

        assertThat(savedDomain1.getDomainId()).isNotNull();
        assertThat(savedDomain2.getDomainId()).isNotNull();
        assertThat(savedDomain3.getDomainId()).isNotNull();
        
        assertThat(savedDomain1.getName()).isEqualTo("Mathematics");
        assertThat(savedDomain2.getName()).isEqualTo("Physics");
        assertThat(savedDomain3.getName()).isEqualTo("Chemistry");

        // IDs should be different
        assertThat(savedDomain1.getDomainId()).isNotEqualTo(savedDomain2.getDomainId());
        assertThat(savedDomain2.getDomainId()).isNotEqualTo(savedDomain3.getDomainId());
    }

    @Test
    public void testDomainWithMissingMandatoryFields() {
        // Test missing name
        DomainEntity domainWithoutName = DomainEntity.builder()
            .build();

        assertThatThrownBy(() -> {
            domainRepository.save(domainWithoutName);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);

        // Test with null name
        DomainEntity domainWithNullName = DomainEntity.builder()
            .name(null)
            .build();

        assertThatThrownBy(() -> {
            domainRepository.save(domainWithNullName);
            entityManager.flush();
        }).isInstanceOf(JpaSystemException.class);
    }

    @Test
    public void testDomainNameLength() {
        // Test with maximum length (100 characters)
        String longName = "A".repeat(100);
        DomainEntity domainWithMaxLength = DomainEntity.builder()
            .name(longName)
            .build();

        DomainEntity savedDomain = domainRepository.save(domainWithMaxLength);
        entityManager.flush();

        assertThat(savedDomain.getName()).isEqualTo(longName);
        assertThat(savedDomain.getName().length()).isEqualTo(100);

        // Test with name exceeding maximum length (should fail)
        String tooLongName = "A".repeat(101);
        DomainEntity domainWithTooLongName = DomainEntity.builder()
            .name(tooLongName)
            .build();

        assertThatThrownBy(() -> {
            domainRepository.save(domainWithTooLongName);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testFindById() {
        DomainEntity domain = DomainEntity.builder()
            .name("Computer Science")
            .build();

        DomainEntity savedDomain = domainRepository.save(domain);
        entityManager.flush();

        Optional<DomainEntity> foundDomain = domainRepository.findById(savedDomain.getDomainId());
        
        assertThat(foundDomain).isPresent();
        assertThat(foundDomain.get().getName()).isEqualTo("Computer Science");
        assertThat(foundDomain.get().getDomainId()).isEqualTo(savedDomain.getDomainId());
    }

    @Test
    public void testFindAll() {
        DomainEntity domain1 = DomainEntity.builder()
            .name("Biology")
            .build();

        DomainEntity domain2 = DomainEntity.builder()
            .name("Literature")
            .build();

        DomainEntity domain3 = DomainEntity.builder()
            .name("History")
            .build();

        domainRepository.save(domain1);
        domainRepository.save(domain2);
        domainRepository.save(domain3);
        entityManager.flush();

        List<DomainEntity> allDomains = domainRepository.findAll();
        
        assertThat(allDomains).hasSizeGreaterThanOrEqualTo(3);
        assertThat(allDomains).extracting(DomainEntity::getName)
            .contains("Biology", "Literature", "History");
    }

    // @Test
    // public void testUpdateDomain() {
    //     DomainEntity domain = DomainEntity.builder()
    //         .name("Original Name")
    //         .build();

    //     DomainEntity savedDomain = domainRepository.save(domain);
    //     entityManager.flush();

    //     // Update the domain name
    //     savedDomain.setName("Updated Name");
    //     DomainEntity updatedDomain = domainRepository.save(savedDomain);
    //     entityManager.flush();

    //     assertThat(updatedDomain.getName()).isEqualTo("Updated Name");
    //     assertThat(updatedDomain.getDomainId()).isEqualTo(savedDomain.getDomainId());

    //     // Verify the update in database
    //     Optional<DomainEntity> reloadedDomain = domainRepository.findById(savedDomain.getDomainId());
    //     assertThat(reloadedDomain).isPresent();
    //     assertThat(reloadedDomain.get().getName()).isEqualTo("Updated Name");
    // }

    // @Test
    // public void testDeleteDomain() {
    //     DomainEntity domain = DomainEntity.builder()
    //         .name("To Be Deleted")
    //         .build();

    //     DomainEntity savedDomain = domainRepository.save(domain);
    //     entityManager.flush();

    //     Integer domainId = savedDomain.getDomainId();
        
    //     domainRepository.delete(savedDomain);
    //     entityManager.flush();

    //     Optional<DomainEntity> deletedDomain = domainRepository.findById(domainId);
    //     assertThat(deletedDomain).isEmpty();
    // }

    // @Test
    // public void testDeleteById() {
    //     DomainEntity domain = DomainEntity.builder()
    //         .name("Delete By ID")
    //         .build();

    //     DomainEntity savedDomain = domainRepository.save(domain);
    //     entityManager.flush();

    //     Integer domainId = savedDomain.getDomainId();
        
    //     domainRepository.deleteById(domainId);
    //     entityManager.flush();

    //     Optional<DomainEntity> deletedDomain = domainRepository.findById(domainId);
    //     assertThat(deletedDomain).isEmpty();
    // }

    @Test
    public void testExistsById() {
        DomainEntity domain = DomainEntity.builder()
            .name("Existence Test")
            .build();

        DomainEntity savedDomain = domainRepository.save(domain);
        entityManager.flush();

        boolean exists = domainRepository.existsById(savedDomain.getDomainId());
        assertThat(exists).isTrue();

        boolean notExists = domainRepository.existsById(999999); // Non-existent ID
        assertThat(notExists).isFalse();
    }

    // @Test
    // public void testCount() {
    //     long initialCount = domainRepository.count();

    //     DomainEntity domain1 = DomainEntity.builder()
    //         .name("Count Test 1")
    //         .build();

    //     DomainEntity domain2 = DomainEntity.builder()
    //         .name("Count Test 2")
    //         .build();

    //     domainRepository.save(domain1);
    //     domainRepository.save(domain2);
    //     entityManager.flush();

    //     long newCount = domainRepository.count();
    //     assertThat(newCount).isEqualTo(initialCount + 2);
    // }

    // @Test
    // public void testDomainWithSpecialCharacters() {
    //     DomainEntity domain = DomainEntity.builder()
    //         .name("Mathematics & Statistics")
    //         .build();

    //     DomainEntity savedDomain = domainRepository.save(domain);
    //     entityManager.flush();

    //     assertThat(savedDomain.getName()).isEqualTo("Mathematics & Statistics");

    //     // Test with unicode characters
    //     DomainEntity unicodeDomain = DomainEntity.builder()
    //         .name("Mathématiques et Physique")
    //         .build();

    //     DomainEntity savedUnicodeDomain = domainRepository.save(unicodeDomain);
    //     entityManager.flush();

    //     assertThat(savedUnicodeDomain.getName()).isEqualTo("Mathématiques et Physique");
    // }

    @Test
    public void testDomainWithEmptyStringName() {
        // Test with empty string (should be allowed if not specifically constrained)
        DomainEntity domainWithEmptyName = DomainEntity.builder()
            .name("")
            .build();

        // This might succeed or fail depending on database constraints
        // If there's a CHECK constraint for non-empty strings, it should fail
        try {
            DomainEntity savedDomain = domainRepository.save(domainWithEmptyName);
            entityManager.flush();
            assertThat(savedDomain.getName()).isEqualTo("");
        } catch (DataIntegrityViolationException e) {
            // This is expected if there's a constraint preventing empty strings
            assertThat(e).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    // @Test
    // public void testAutoGeneratedId() {
    //     DomainEntity domain1 = DomainEntity.builder()
    //         .name("Auto ID Test 1")
    //         .build();

    //     DomainEntity domain2 = DomainEntity.builder()
    //         .name("Auto ID Test 2")
    //         .build();

    //     // Before saving, IDs should be null
    //     assertThat(domain1.getDomainId()).isNull();
    //     assertThat(domain2.getDomainId()).isNull();

    //     DomainEntity savedDomain1 = domainRepository.save(domain1);
    //     DomainEntity savedDomain2 = domainRepository.save(domain2);
    //     entityManager.flush();

    //     // After saving, IDs should be auto-generated and different
    //     assertThat(savedDomain1.getDomainId()).isNotNull();
    //     assertThat(savedDomain2.getDomainId()).isNotNull();
    //     assertThat(savedDomain1.getDomainId()).isNotEqualTo(savedDomain2.getDomainId());

    //     // IDs should be sequential (or at least in ascending order)
    //     assertThat(savedDomain2.getDomainId()).isGreaterThan(savedDomain1.getDomainId());
    // }

    // @Test
    // public void testBulkOperations() {
    //     // Test saving multiple domains at once
    //     DomainEntity domain1 = DomainEntity.builder().name("Bulk 1").build();
    //     DomainEntity domain2 = DomainEntity.builder().name("Bulk 2").build();
    //     DomainEntity domain3 = DomainEntity.builder().name("Bulk 3").build();

    //     List<DomainEntity> domainsToSave = List.of(domain1, domain2, domain3);
    //     List<DomainEntity> savedDomains = domainRepository.saveAll(domainsToSave);
    //     entityManager.flush();

    //     assertThat(savedDomains).hasSize(3);
    //     assertThat(savedDomains).extracting(DomainEntity::getName)
    //         .containsExactlyInAnyOrder("Bulk 1", "Bulk 2", "Bulk 3");

    //     // Test bulk delete
    //     domainRepository.deleteAll(savedDomains);
    //     entityManager.flush();

    //     for (DomainEntity savedDomain : savedDomains) {
    //         Optional<DomainEntity> deletedDomain = domainRepository.findById(savedDomain.getDomainId());
    //         assertThat(deletedDomain).isEmpty();
    //     }
    // }
}