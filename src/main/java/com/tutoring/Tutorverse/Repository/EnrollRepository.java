package com.tutoring.Tutorverse.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.tutoring.Tutorverse.Model.EnrollmentEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollRepository extends JpaRepository<EnrollmentEntity, UUID> {
    List<EnrollmentEntity> findByStudentStudentId(UUID studentID);
    boolean existsByStudentStudentIdAndModuleModuleId(UUID studentId, UUID moduleId);
    Optional<EnrollmentEntity> findByStudentStudentIdAndModuleModuleId(UUID studentId, UUID moduleId);


    // Fixed method to get isPaid value
    @Query("SELECT e.isPaid FROM EnrollmentEntity e WHERE e.student.studentId = :studentId AND e.module.moduleId = :moduleId")
    Optional<Boolean> findIsPaidByStudentIdAndModuleId(@Param("studentId") UUID studentId, @Param("moduleId") UUID moduleId);

    // Alternative method using method naming convention
    @Query("SELECT e.isPaid FROM EnrollmentEntity e WHERE e.student.studentId = ?1 AND e.module.moduleId = ?2")
    Optional<Boolean> getIsPaidByStudentStudentIdAndModuleModuleId(UUID studentId, UUID moduleId);
    
    Integer countByModuleModuleId(UUID moduleId);
    long count();
}
