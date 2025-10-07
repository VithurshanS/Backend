package com.tutoring.Tutorverse.Repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tutoring.Tutorverse.Model.StudentEntity;

public interface StudentProfileRepository extends JpaRepository<StudentEntity, UUID>{

    List<StudentEntity> findByIsActiveTrue();

    List<StudentEntity> findByIsActiveFalse();

}
