package com.tutoring.Tutorverse.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Model.Role;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;
import com.tutoring.Tutorverse.TestUtils.TestDataHelper;

import jakarta.persistence.PersistenceException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class UserRepoTests extends BaseRepositoryTest {

    @Autowired
    private userRepository userRepo;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    public void setUp() {
        // Ensure standard roles exist before each test
        createStandardRoles();
    }
    @Test
    public void testSaveAndFindUser() {
        String uniqueEmail = "test" + System.currentTimeMillis() + "@example.com";
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setName("Test User");
        user.setPassword("password");
        user.setRole(roleRepository.findByName("STUDENT").orElse(null));
        User savedUser = userRepo.save(user);
        entityManager.flush();

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(uniqueEmail);
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getRole().getName()).isEqualTo("STUDENT");
    }

    @Test
    public void testExistsByEmail() {
        // Given - Create a unique role first
        String uniqueRoleName = "TEST_TUTOR_" + System.currentTimeMillis();
        Role role = new Role(uniqueRoleName);
        Role savedRole = roleRepository.save(role);
        entityManager.flush();

        // Create a unique user
        String uniqueEmail = "exists" + System.currentTimeMillis() + "@example.com";
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setName("Exists User");
        user.setPassword("password");
        user.setRole(savedRole);

        // When
        userRepo.save(user);
        entityManager.flush();
        boolean exists = userRepo.existsByEmail(uniqueEmail);
        boolean notExists = userRepo.existsByEmail("nonexistent" + System.currentTimeMillis() + "@example.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    public void testFindByEmail() {
        entityManager.flush();

        String uniqueEmail = "findme" + System.currentTimeMillis() + "@example.com";
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setName("Find Me User");
        user.setPassword("password");
        user.setRole(roleRepository.findByName("ADMIN").orElse(null));

        userRepo.save(user);
        entityManager.flush();
        Optional<User> foundUser = userRepo.findByEmail(uniqueEmail);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(uniqueEmail);
        assertThat(foundUser.get().getName()).isEqualTo("Find Me User");
        assertThat(foundUser.get().getRole().getName()).isEqualTo("ADMIN");
    }

    @Test
    public void testFindByProviderid() {
  
    
        String uniqueEmail = "oauth" + System.currentTimeMillis() + "@example.com";
        String uniqueProviderId = "google_" + System.currentTimeMillis();
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setName("OAuth User");
        user.setProviderid(uniqueProviderId);
        user.setRole(roleRepository.findByName("STUDENT").orElse(null));

        userRepo.save(user);
        entityManager.flush();
        Optional<User> foundUser = userRepo.findByProviderid(uniqueProviderId);


        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getProviderid()).isEqualTo(uniqueProviderId);
        assertThat(foundUser.get().getEmail()).isEqualTo(uniqueEmail);
        assertThat(foundUser.get().getRole().getName()).isEqualTo("STUDENT");
    }

    @Test
    public void testDuplicateEmailConstraint() {
        // Given
        String duplicateEmail = "duplicate" + System.currentTimeMillis() + "@example.com";
        

        User user1 = new User();
        user1.setEmail(duplicateEmail);
        user1.setName("User One");
        user1.setPassword("password1");
        user1.setRole(roleRepository.findByName("TUTOR").orElse(null));
        userRepo.save(user1);
        entityManager.flush();

        User user2 = new User();
        user2.setEmail(duplicateEmail); 
        user2.setName("User Two");
        user2.setPassword("password2");
        user2.setRole(roleRepository.findByName("TUTOR").orElse(null));

        assertThatThrownBy(() -> {
            userRepo.save(user2);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    public void testNullEmailConstraint() {

        // Create user with null email
        User user = new User();
        user.setEmail(null); // Null email
        user.setName("Null Email User");
        user.setPassword("password");
        user.setRole(roleRepository.findByName("STUDENT").orElse(null));

        // When & Then - Should throw constraint violation exception
        assertThatThrownBy(() -> {
            userRepo.save(user);
            entityManager.flush(); // This forces the constraint check
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    public void testNullRoleConstraint() {
        // Create user with null role
        String uniqueEmail = "norole" + System.currentTimeMillis() + "@example.com";
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setName("No Role User");
        user.setPassword("password");
        user.setRole(null); // Null role

        // When & Then - Should throw constraint violation exception
        assertThatThrownBy(() -> {
            userRepo.save(user);
            entityManager.flush(); // This forces the constraint check
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    public void testDuplicateProviderIdConstraint() {
        // Given
        String duplicateProviderId = "google_dup_" + System.currentTimeMillis();

        User user1 = new User();
        user1.setEmail("user1_" + System.currentTimeMillis() + "@example.com");
        user1.setName("User One");
        user1.setProviderid(duplicateProviderId);
        user1.setRole(roleRepository.findByName("STUDENT").orElse(null));
        userRepo.save(user1);
        entityManager.flush();

        User user2 = new User();
        user2.setEmail("user2_" + System.currentTimeMillis() + "@example.com");
        user2.setName("User Two");
        user2.setProviderid(duplicateProviderId); // Same provider ID as user1
        user2.setRole(roleRepository.findByName("TUTOR").orElse(null));

        // When & Then - Should throw constraint violation exception
        assertThatThrownBy(() -> {
            userRepo.save(user2);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    public void testNullProviderIdAllowed() {
        // Create user with null provider ID
        String uniqueEmail = "nullprovider" + System.currentTimeMillis() + "@example.com";
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setName("Null Provider User");
        user.setPassword("password");
        user.setProviderid(null); // Null provider ID
        user.setRole(roleRepository.findByName("TUTOR").orElse(null));

        // When
        User savedUser = userRepo.save(user);
        entityManager.flush();

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(uniqueEmail);
        assertThat(savedUser.getProviderid()).isNull();
        assertThat(savedUser.getRole().getName()).isEqualTo("TUTOR");
    }



}