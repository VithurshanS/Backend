package com.tutoring.Tutorverse.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tutoring.Tutorverse.Model.EnrollmentEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailRepository extends JpaRepository<EnrollmentEntity, UUID> {

    @Query("SELECT u.email, u.name FROM User u " +
            "JOIN EnrollmentEntity e ON u.id = e.student.user.id " +
            "WHERE e.module.moduleId = :moduleId")
    List<Object[]> findEmailsAndFirstNamesByModuleId(@Param("moduleId") UUID moduleId);

    @Query("SELECT u.email FROM User u " +
            "JOIN EnrollmentEntity e ON u.id = e.student.user.id " +
            "WHERE e.module.moduleId = :moduleId")
    List<String> findEmailsByModuleId(@Param("moduleId") UUID moduleId);


}
