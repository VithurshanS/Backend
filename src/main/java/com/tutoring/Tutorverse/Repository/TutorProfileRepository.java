package com.tutoring.Tutorverse.Repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tutoring.Tutorverse.Model.TutorEntity;


public interface TutorProfileRepository extends JpaRepository<TutorEntity, UUID> {

}
