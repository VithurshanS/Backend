package com.tutoring.Tutorverse.TestUtils;

import com.tutoring.Tutorverse.Model.Role;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for repository tests that provides common setup and helper methods
 */
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public abstract class BaseRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;

    @Autowired
    protected RoleRepository roleRepository;

    protected Role studentRole;
    protected Role tutorRole;
    protected Role adminRole;

    // @BeforeEach
    // public void setUpBaseData() {
    //     // // Clean existing data first
    //     // cleanupData();
        
    //     // Create standard roles
    //     createStandardRoles();
    // }

    // protected void cleanupData() {
    //     // Clear all data to ensure clean state
    //     entityManager.clear();
    // }

    protected void createStandardRoles() {
        // Create roles only if they don't exist
        if (!roleRepository.existsByName(TestDataHelper.STUDENT_ROLE)) {
            studentRole = TestDataHelper.createAndPersistRole(entityManager, TestDataHelper.STUDENT_ROLE);
        } else {
            studentRole = roleRepository.findByName(TestDataHelper.STUDENT_ROLE).orElse(null);
        }

        if (!roleRepository.existsByName(TestDataHelper.TUTOR_ROLE)) {
            tutorRole = TestDataHelper.createAndPersistRole(entityManager, TestDataHelper.TUTOR_ROLE);
        } else {
            tutorRole = roleRepository.findByName(TestDataHelper.TUTOR_ROLE).orElse(null);
        }

        if (!roleRepository.existsByName(TestDataHelper.ADMIN_ROLE)) {
            adminRole = TestDataHelper.createAndPersistRole(entityManager, TestDataHelper.ADMIN_ROLE);
        } else {
            adminRole = roleRepository.findByName(TestDataHelper.ADMIN_ROLE).orElse(null);
        }
        // if (!roleRepository.existsByName("STUDEN")) {
        //     adminRole = TestDataHelper.createAndPersistRole(entityManager, "STUDEN");
        // } else {
        //     adminRole = roleRepository.findByName("STUDEN").orElse(null);
        // }
    }

    // protected void createTestUser(String email, String name, String password, Role role) {
    //     TestDataHelper.createUser(email, name, password, role);
    // }

    // // Helper methods for creating test users
    // protected User createTestStudent(String email, String name) {
    //     return TestDataHelper.createUser(email, name, "password", studentRole);
    // }

    // protected User createTestTutor(String email, String name) {
    //     return TestDataHelper.createUser(email, name, "password", tutorRole);
    // }

    // protected User createTestAdmin(String email, String name) {
    //     return TestDataHelper.createUser(email, name, "password", adminRole);
    // }

    // protected void createTestUser(String email, String name,String pass, Role role,String providerId) {
    //     TestDataHelper.createAndPersistUser(entityManager, email, name, pass, role, providerId);
    // }

    // // Helper methods for persisting test data
    protected User persistTestStudent(String email, String name) {
        return TestDataHelper.createAndPersistUser(entityManager, email, name, "password", studentRole,null);
    }

    protected User persistTestTutor(String email, String name) {
        return TestDataHelper.createAndPersistUser(entityManager, email, name, "password", tutorRole,null);
    }

    protected User persistTestAdmin(String email, String name) {
        return TestDataHelper.createAndPersistUser(entityManager, email, name, "password", adminRole,null);
    }
}