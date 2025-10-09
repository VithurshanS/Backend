package com.tutoring.Tutorverse.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class StudentRepoTests extends BaseRepositoryTest {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private userRepository userRepository;

    @BeforeEach
    public void setUp() {
        // Ensure standard roles exist before each test
        createStandardRoles();
        persistTestStudent("teststudent@example.com", "Test Student");
        entityManager.flush();
    }

    @Test
    public void testStudentCreateProfile() {
        // Test logic for creating a student profile
        Optional<User> student = userRepository.findByEmail("teststudent@example.com");
        assert(student.isPresent());
        assert(student.get().getRole().getName().equals("STUDENT"));
        
        StudentEntity studentProfile = new StudentEntity();
        studentProfile.setFirstName("Test");
        studentProfile.setLastName("Student");
        studentProfile.setUser(student.get());
        studentProfile.setAddress("address");
        studentProfile.setCity("city");
        studentProfile.setCountry("country");
        studentProfile.setPhoneNumber("1234567890");
        studentProfile.setBio("bio");
        studentProfile.setIsActive(true);
        
        StudentEntity savedProfile = studentProfileRepository.save(studentProfile);
        assert(savedProfile.getUser().getEmail().equals("teststudent@example.com"));
    }

    @Test
    public void testFindStudent() {
        Optional<User> student = userRepository.findByEmail("teststudent@example.com");
        assert(student.isPresent());
        assert(student.get().getRole().getName().equals("STUDENT"));
    }

    @Test
    public void testStudentProfileByTutor() {
        persistTestTutor("testtutor@example.com", "Test Tutor");
        entityManager.flush();
        Optional<User> tutor = userRepository.findByEmail("testtutor@example.com");
        assert(tutor.isPresent());
        assert(tutor.get().getRole().getName().equals("TUTOR"));
        
        StudentEntity studentProfile = new StudentEntity();
        studentProfile.setFirstName("Test");
        studentProfile.setLastName("Tutor");
        studentProfile.setUser(tutor.get());
        studentProfile.setAddress("address");
        studentProfile.setCity("city");
        studentProfile.setCountry("country");
        studentProfile.setPhoneNumber("1234567891");
        studentProfile.setBio("bio");
        studentProfile.setIsActive(true);
        
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile);
            entityManager.flush();
        }).isInstanceOf(InvalidDataAccessApiUsageException.class);
    }

    @Test
    public void testStudentProfileWithAdmin() {
        persistTestAdmin("testadmin@example.com", "Test Admin");
        entityManager.flush();
        Optional<User> admin = userRepository.findByEmail("testadmin@example.com");
        assert(admin.isPresent());
        assert(admin.get().getRole().getName().equals("ADMIN"));

        StudentEntity studentProfile = new StudentEntity();
        studentProfile.setFirstName("Test");
        studentProfile.setLastName("Admin");
        studentProfile.setUser(admin.get());
        studentProfile.setAddress("address");
        studentProfile.setCity("city");
        studentProfile.setCountry("country");
        studentProfile.setPhoneNumber("1234567892");
        studentProfile.setBio("bio");
        studentProfile.setIsActive(true);
        
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile);
            entityManager.flush();
        }).isInstanceOf(InvalidDataAccessApiUsageException.class);
    }

    @Test
    public void testStudentProfileWithNullUser() {
        StudentEntity studentProfile = new StudentEntity();
        studentProfile.setFirstName("Test");
        studentProfile.setLastName("NullUser");
        studentProfile.setUser(null);
        studentProfile.setAddress("address");
        studentProfile.setCity("city");
        studentProfile.setCountry("country");
        studentProfile.setPhoneNumber("1234567893");
        studentProfile.setBio("bio");
        studentProfile.setIsActive(true);
        
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile);
            entityManager.flush();
        }).isInstanceOf(JpaSystemException.class);
    }

    @Test
    public void testStudentProfileWithMissingMandatoryFields() {
        Optional<User> student = userRepository.findByEmail("teststudent@example.com");
        assert(student.isPresent());
        assert(student.get().getRole().getName().equals("STUDENT"));

        StudentEntity studentProfile = new StudentEntity();
        studentProfile.setLastName("Student");
        studentProfile.setUser(student.get());
        studentProfile.setAddress("address");
        studentProfile.setCity("city");
        studentProfile.setCountry("country");
        studentProfile.setPhoneNumber("1234567890");
        studentProfile.setBio("bio");
        
        // Test missing firstName
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        
        // Test missing lastName
        studentProfile.setFirstName("Test");
        studentProfile.setLastName(null);
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        
        // Test missing address
        studentProfile.setLastName("Student");
        studentProfile.setAddress(null);
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        
        // Test missing city
        studentProfile.setAddress("address");
        studentProfile.setCity(null);
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        
        // Test missing country
        studentProfile.setCity("city");
        studentProfile.setCountry(null);
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        
        // Test missing phoneNumber
        studentProfile.setCountry("country");
        studentProfile.setPhoneNumber(null);
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    public void testStudentProfileWithDuplicateUser() {
        Optional<User> student = userRepository.findByEmail("teststudent@example.com");
        assert(student.isPresent());
        assert(student.get().getRole().getName().equals("STUDENT"));

        StudentEntity studentProfile1 = new StudentEntity();
        studentProfile1.setFirstName("Test");
        studentProfile1.setLastName("Student");
        studentProfile1.setUser(student.get());
        studentProfile1.setAddress("address");
        studentProfile1.setCity("city");
        studentProfile1.setCountry("country");
        studentProfile1.setPhoneNumber("1234567890");
        studentProfile1.setBio("bio");
        studentProfile1.setIsActive(true);
        
        studentProfileRepository.save(studentProfile1);
        entityManager.flush();

        StudentEntity studentProfile2 = new StudentEntity();
        studentProfile2.setFirstName("Test");
        studentProfile2.setLastName("Student");
        studentProfile2.setUser(student.get());
        studentProfile2.setAddress("address");
        studentProfile2.setCity("city");
        studentProfile2.setCountry("country");
        studentProfile2.setPhoneNumber("2234567890"); // Different phone number
        studentProfile2.setBio("bio");
        studentProfile2.setIsActive(true);
        
        assertThatThrownBy(() -> {
            studentProfileRepository.save(studentProfile2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    // @Test
    // @Disabled("Ignoring this test case as requested")
    // public void testStudentProfileWithDuplicatePhoneNumber() {
    //     Optional<User> student = userRepository.findByEmail("teststudent@example.com");
    //     assert(student.isPresent());
    //     assert(student.get().getRole().getName().equals("STUDENT"));

    //     StudentEntity studentProfile1 = new StudentEntity();
    //     studentProfile1.setFirstName("Test");
    //     studentProfile1.setLastName("Student");
    //     studentProfile1.setUser(student.get());
    //     studentProfile1.setAddress("address");
    //     studentProfile1.setCity("city");
    //     studentProfile1.setCountry("country");
    //     studentProfile1.setPhoneNumber("1234567890");
    //     studentProfile1.setBio("bio");
    //     studentProfile1.setIsActive(true);
        
    //     studentProfileRepository.save(studentProfile1);
    //     entityManager.flush();
        
    //     persistTestStudent("teststudent2@example.com", "Test2 Student");
    //     entityManager.flush();
    //     Optional<User> student2 = userRepository.findByEmail("teststudent2@example.com");
    //     assert(student2.isPresent());
    //     assert(student2.get().getRole().getName().equals("STUDENT"));
        
    //     StudentEntity studentProfile2 = new StudentEntity();
    //     studentProfile2.setFirstName("Test");
    //     studentProfile2.setLastName("Student");
    //     studentProfile2.setUser(student2.get());
    //     studentProfile2.setAddress("address");
    //     studentProfile2.setCity("city");
    //     studentProfile2.setCountry("country");
    //     studentProfile2.setPhoneNumber("1234567890"); // Same phone number
    //     studentProfile2.setBio("bio");
    //     studentProfile2.setIsActive(true);
        
    //     // Note: This test assumes phone_number has a unique constraint
    //     // If not, this test should be removed or the constraint should be added
    //     assertThatThrownBy(() -> {
    //         studentProfileRepository.save(studentProfile2);
    //         entityManager.flush();
    //     }).isInstanceOf(PersistenceException.class);
    // }

    // @Test
    // public void testFindByFirstNameIgnoreCaseConstraint() {
    //     Optional<User> student = userRepository.findByEmail("teststudent@example.com");
    //     assert(student.isPresent());
    //     assert(student.get().getRole().getName().equals("STUDENT"));

    //     StudentEntity studentProfile = new StudentEntity();
    //     studentProfile.setFirstName("Test");
    //     studentProfile.setLastName("Student");
    //     studentProfile.setUser(student.get());
    //     studentProfile.setAddress("address");
    //     studentProfile.setCity("city");
    //     studentProfile.setCountry("country");
    //     studentProfile.setPhoneNumber("1234567890");
    //     studentProfile.setBio("bio");
    //     studentProfile.setIsActive(true);
        
    //     studentProfileRepository.save(studentProfile);
    //     entityManager.flush();

    //     Optional<StudentEntity> foundStudents = studentProfileRepository.findByFirstNameContainingIgnoreCase("test");
    //     assertThat(foundStudents).isPresent();
    //     assertThat(foundStudents.get().getFirstName()).isEqualTo("Test");
    // }
}
