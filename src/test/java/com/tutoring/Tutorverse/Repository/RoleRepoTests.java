package com.tutoring.Tutorverse.Repository;

import org.junit.jupiter.api.Test;
import org.hibernate.validator.constraints.pl.REGON;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.tutoring.Tutorverse.Model.Role;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class RoleRepoTests {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    // @BeforeEach
    // public void setUp() {
    //     // Clean up any existing data for this test
    //     roleRepository.deleteAll();
    //     entityManager.flush();
    //     entityManager.clear();
    // }
    

    @Test
    public void testSaveRole() {

        String uniqueRoleName = "STUDENT";
        Role role = new Role(uniqueRoleName);
        Role savedRole = roleRepository.save(role);
        entityManager.flush();
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo(uniqueRoleName);
    }

    

    @Test
    public void testFindByName() {
        Role role = new Role("STUDENT");
        roleRepository.save(role);
        entityManager.flush();
        Optional<Role> foundRole = roleRepository.findByName("STUDENT");
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("STUDENT");
    }

    @Test
    public void testFindByName_NotFound() {
        String nonExistentRoleName = "ADMIN";
        Optional<Role> foundRole = roleRepository.findByName(nonExistentRoleName);
        assertThat(foundRole).isEmpty();
    }

    @Test
    public void testExistsByName() {
        // Given
        Role role = new Role("STUDENT");
        roleRepository.save(role);
        entityManager.flush();

        boolean exists = roleRepository.existsByName("STUDENT");
        boolean notExists = roleRepository.existsByName("NONEXISTENT");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }


    @Test
    public void testUniqueConstraintViolation() {
        // Given
        Role role1 = new Role("DUPLICATE");
        Role role2 = new Role("DUPLICATE");

        roleRepository.save(role1);
        entityManager.flush();

        assertThatThrownBy(() -> {
            roleRepository.save(role2);
            entityManager.flush(); 
        }).isInstanceOf(DataIntegrityViolationException.class);
    }



}
