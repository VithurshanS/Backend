package com.tutoring.Tutorverse.Repository;

import com.tutoring.Tutorverse.Model.RatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<RatingEntity, UUID> {
    
    // Find rating by enrollment ID
    Optional<RatingEntity> findByEnrolmentId(UUID enrolmentId);
    
    // Check if a rating exists for a specific enrollment
    boolean existsByEnrolmentId(UUID enrolmentId);
    
    // Legacy join-based (still works if enrollment lazy loaded needed elsewhere)
    @Query("SELECT r FROM RatingEntity r JOIN r.enrollment e WHERE e.module.moduleId = :moduleId")
    List<RatingEntity> findByModuleId(@Param("moduleId") UUID moduleId);

    // Find all ratings by a specific student
    @Query("SELECT r FROM RatingEntity r JOIN r.enrollment e WHERE e.student.studentId = :studentId")
    List<RatingEntity> findByStudentId(@Param("studentId") UUID studentId);

    // New direct finder using stored moduleId column
    List<RatingEntity> findAllByModuleId(UUID moduleId);

    @Query("SELECT COALESCE(AVG(r.rating),0) FROM RatingEntity r")
    Double findPlatformAverageRating();
}
