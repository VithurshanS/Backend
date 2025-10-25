package com.tutoring.Tutorverse.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import com.tutoring.Tutorverse.Model.TutorEntity;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Model.TutorEntity.Gender;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class TutorRepoTests extends BaseRepositoryTest {

    @Autowired
    private TutorProfileRepository tutorProfileRepository;

    @Autowired
    private userRepository userRepository;

    @BeforeEach
    public void setUp() {
        // Ensure standard roles exist before each test
        createStandardRoles();
        persistTestTutor("testtutor@example.com", "Test Tutor");
        entityManager.flush();
    }

    @Test
    public void testTutorCreateProfile() {
        // Implement test logic for creating a tutor profile
        Optional<User> tutor = userRepository.findByEmail("testtutor@example.com");
        assert(tutor.isPresent());
        assert(tutor.get().getRole().getName().equals("TUTOR"));
        TutorEntity tutorProfile = new TutorEntity();
        tutorProfile.setFirstName("Test");
        tutorProfile.setLastName("Tutor");
        tutorProfile.setUser(tutor.get());
        tutorProfile.setAddress("address");
        tutorProfile.setCity("city");
        tutorProfile.setCountry("country");
        tutorProfile.setGender(Gender.MALE);
        tutorProfile.setBio("bio");
        tutorProfile.setPhoneNo("1234567890");
        TutorEntity savedProfile = tutorProfileRepository.save(tutorProfile);
        assert(savedProfile.getUser().getEmail().equals("testtutor@example.com"));

    }

    @Test
    public void testFindTutor() {
        Optional<User> tutor = userRepository.findByEmail("testtutor@example.com");
        assert(tutor.isPresent());
        assert(tutor.get().getRole().getName().equals("TUTOR"));
    }

    @Test
    public void testTutorProfilebyStudent(){
        persistTestStudent("teststudent@example.com", "Test Student");
        entityManager.flush();
        Optional<User> student = userRepository.findByEmail("teststudent@example.com");
        assert(student.isPresent());
        assert(student.get().getRole().getName().equals("STUDENT"));
        
        TutorEntity tutorProfile = new TutorEntity();
        tutorProfile.setFirstName("Test");
        tutorProfile.setLastName("Student");
        tutorProfile.setUser(student.get());
        tutorProfile.setAddress("address");
        tutorProfile.setCity("city");
        tutorProfile.setCountry("country");
        tutorProfile.setGender(Gender.MALE);
        tutorProfile.setBio("bio");
        tutorProfile.setPhoneNo("1234567891");    
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(InvalidDataAccessApiUsageException.class);
    }

    @Test
    public void testTutorProfileWithAdmin(){
        persistTestAdmin("testadmin@example.com", "Test Admin");
        entityManager.flush();
        Optional<User> admin = userRepository.findByEmail("testadmin@example.com");
        assert(admin.isPresent());
        assert(admin.get().getRole().getName().equals("ADMIN"));

        TutorEntity tutorProfile = new TutorEntity();
        tutorProfile.setFirstName("Test");
        tutorProfile.setLastName("Admin");
        tutorProfile.setUser(admin.get());
        tutorProfile.setAddress("address");
        tutorProfile.setCity("city");
        tutorProfile.setCountry("country");
        tutorProfile.setGender(Gender.MALE);
        tutorProfile.setBio("bio");
        tutorProfile.setPhoneNo("1234567892");
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(InvalidDataAccessApiUsageException.class);
    }

    @Test
    public void testTutorProfileWithNullUser(){
        TutorEntity tutorProfile = new TutorEntity();
        tutorProfile.setFirstName("Test");
        tutorProfile.setLastName("NullUser");
        tutorProfile.setUser(null);
        tutorProfile.setAddress("address");
        tutorProfile.setCity("city");
        tutorProfile.setCountry("country");
        tutorProfile.setGender(Gender.MALE);
        tutorProfile.setBio("bio");
        tutorProfile.setPhoneNo("1234567893");
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(JpaSystemException.class);
    }

    @Test
    public void testTutorProfileWithMissingMandatoryFields(){
        Optional<User> tutor = userRepository.findByEmail("testtutor@example.com");
        assert(tutor.isPresent());
        assert(tutor.get().getRole().getName().equals("TUTOR"));

        TutorEntity tutorProfile = new TutorEntity();
        tutorProfile.setLastName("Tutor");
        tutorProfile.setUser(tutor.get());
        tutorProfile.setAddress("address");
        tutorProfile.setCity("city");
        tutorProfile.setCountry("country");
        tutorProfile.setGender(Gender.MALE);
        tutorProfile.setBio("bio");
        tutorProfile.setPhoneNo("1234567890");
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        tutorProfile.setFirstName("Test");
        tutorProfile.setLastName(null);
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        tutorProfile.setLastName("Tutor");
        tutorProfile.setAddress(null);
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        tutorProfile.setAddress("address");
        tutorProfile.setCity(null);
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        tutorProfile.setCity("city");
        tutorProfile.setCountry(null);
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        tutorProfile.setCountry("country");
        tutorProfile.setGender(null);
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        tutorProfile.setGender(Gender.MALE);
        tutorProfile.setPhoneNo(null);
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);

    }

    @Test
    public void testTutorprofilewithDuplicateUser(){
        Optional<User> tutor = userRepository.findByEmail("testtutor@example.com");
        assert(tutor.isPresent());
        assert(tutor.get().getRole().getName().equals("TUTOR"));

        TutorEntity tutorProfile1 = new TutorEntity();
        tutorProfile1.setFirstName("Test");
        tutorProfile1.setLastName("Tutor");
        tutorProfile1.setUser(tutor.get());
        tutorProfile1.setAddress("address");
        tutorProfile1.setCity("city");
        tutorProfile1.setCountry("country");
        tutorProfile1.setGender(Gender.MALE);
        tutorProfile1.setBio("bio");
        tutorProfile1.setPhoneNo("1234567890");
        tutorProfileRepository.save(tutorProfile1);
        entityManager.flush();

        TutorEntity tutorProfile2 = new TutorEntity();
        tutorProfile2.setFirstName("Test");
        tutorProfile2.setLastName("Tutor");
        tutorProfile2.setUser(tutor.get());
        tutorProfile2.setAddress("address");
        tutorProfile2.setCity("city");
        tutorProfile2.setCountry("country");
        tutorProfile2.setGender(Gender.MALE);
        tutorProfile2.setBio("bio");
        tutorProfile2.setPhoneNo("2234567890");
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testTutorProfileWithDuplicatePhoneNo(){
        Optional<User> tutor = userRepository.findByEmail("testtutor@example.com");
        assert(tutor.isPresent());
        assert(tutor.get().getRole().getName().equals("TUTOR"));

        TutorEntity tutorProfile1 = new TutorEntity();
        tutorProfile1.setFirstName("Test");
        tutorProfile1.setLastName("Tutor");
        tutorProfile1.setUser(tutor.get());
        tutorProfile1.setAddress("address");
        tutorProfile1.setCity("city");
        tutorProfile1.setCountry("country");
        tutorProfile1.setGender(Gender.MALE);
        tutorProfile1.setBio("bio");
        tutorProfile1.setPhoneNo("1234567890");
        tutorProfileRepository.save(tutorProfile1);
        entityManager.flush();
        persistTestTutor("testtutor2@example.com", "Test2 Tutor");
        entityManager.flush();
        Optional<User> tutor2 = userRepository.findByEmail("testtutor2@example.com");
        assert(tutor2.isPresent());
        assert(tutor2.get().getRole().getName().equals("TUTOR"));
        TutorEntity tutorProfile2 = new TutorEntity();
        tutorProfile2.setFirstName("Test");
        tutorProfile2.setLastName("Tutor");
        tutorProfile2.setUser(tutor2.get());
        tutorProfile2.setAddress("address");
        tutorProfile2.setCity("city");
        tutorProfile2.setCountry("country");
        tutorProfile2.setGender(Gender.MALE);
        tutorProfile2.setBio("bio");
        tutorProfile2.setPhoneNo("1234567890");
        assertThatThrownBy(() -> {
            tutorProfileRepository.save(tutorProfile2);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    public void testFindByFirstNamebyIgnoreCaseConstraint(){
        Optional<User> tutor = userRepository.findByEmail("testtutor@example.com");
        assert(tutor.isPresent());
        assert(tutor.get().getRole().getName().equals("TUTOR"));

        TutorEntity tutorProfile = new TutorEntity();
        tutorProfile.setFirstName("Test");
        tutorProfile.setLastName("Tutor");
        tutorProfile.setUser(tutor.get());
        tutorProfile.setAddress("address");
        tutorProfile.setCity("city");
        tutorProfile.setCountry("country");
        tutorProfile.setGender(Gender.MALE);
        tutorProfile.setBio("bio");
        tutorProfile.setPhoneNo("1234567890");
        tutorProfileRepository.save(tutorProfile);
        entityManager.flush();

        Optional<TutorEntity> foundTutors = tutorProfileRepository.findByFirstNameContainingIgnoreCase("test");
        assertThat(foundTutors).isPresent();
        assertThat(foundTutors.get().getFirstName()).isEqualTo("Test");
    }
}