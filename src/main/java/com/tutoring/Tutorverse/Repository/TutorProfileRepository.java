package com.tutoring.Tutorverse.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tutoring.Tutorverse.Model.TutorEntity;


public interface TutorProfileRepository extends JpaRepository<TutorEntity, UUID> {


    Optional<TutorEntity> findByFirstNameContainingIgnoreCase(String query);

    List<TutorEntity> findByStatus(TutorEntity.Status status);

    long countByCreatedAtBetween(LocalDateTime lastStart, LocalDateTime lastEnd);

    long count();
    long countByStatus(TutorEntity.Status status);

}
