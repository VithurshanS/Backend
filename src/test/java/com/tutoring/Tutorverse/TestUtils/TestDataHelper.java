package com.tutoring.Tutorverse.TestUtils;

import com.tutoring.Tutorverse.Model.Role;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.RoleRepository;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Utility class for creating test data consistently across tests
 */
public class TestDataHelper {

    /**
     * Creates a Role entity with the given name
     */

    
    public static Role createRole(String name) {
        return new Role(name);
    }

    /**
     * Creates and persists a Role entity using TestEntityManager
     */
    public static Role createAndPersistRole(TestEntityManager entityManager, String name) {
        Role role = new Role(name);
        return entityManager.persistAndFlush(role);
    }

    /**
     * Creates multiple roles and persists them using TestEntityManager
     */
    public static Role[] createAndPersistRoles(TestEntityManager entityManager, String... names) {
        Role[] roles = new Role[names.length];
        for (int i = 0; i < names.length; i++) {
            roles[i] = entityManager.persistAndFlush(new Role(names[i]));
        }
        return roles;
    }

    /**
     * Creates roles if they don't already exist in the repository
     */
    public static void createRolesIfNotExist(RoleRepository roleRepository, String... roleNames) {
        for (String roleName : roleNames) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(new Role(roleName));
            }
        }
    }

    /**
     * Creates a User entity with basic information
     */
    public static User createUser(String email, String name, String password, Role role, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password);
        user.setRole(role);
        user.setProviderid(providerId);
        return user;
    }

    /**
     * Creates a User with OAuth provider information
     */
    public static User createUserWithProvider(String email, String name, String providerId, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProviderid(providerId);
        user.setRole(role);
        return user;
    }

    /**
     * Creates and persists a User entity using TestEntityManager
     */
    public static User createAndPersistUser(TestEntityManager entityManager, String email, String name, String password, Role role, String providerId) {
        User user = createUser(email, name, password, role, providerId);
        return entityManager.persistAndFlush(user);
    }

    // Common role names as constants
    public static final String STUDENT_ROLE = "STUDENT";
    public static final String TUTOR_ROLE = "TUTOR";
    public static final String ADMIN_ROLE = "ADMIN";
}