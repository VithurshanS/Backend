package com.tutoring.Tutorverse.Repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tutoring.Tutorverse.Model.EnrollmentEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollRepository extends JpaRepository<EnrollmentEntity, UUID> {
    List<EnrollmentEntity> findByStudentStudentId(UUID studentID);


    // Custom query methods (if needed) can be defined here
}
