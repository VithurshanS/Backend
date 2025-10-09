package com.tutoring.Tutorverse.Repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Model.User;

public interface StudentProfileRepository extends JpaRepository<StudentEntity, UUID>{

    Optional<StudentEntity> findByUser(User user);
    boolean existsByUser(User user);

}
