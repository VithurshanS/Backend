package com.tutoring.Tutorverse.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tutoring.Tutorverse.Model.ModuelDescription;

@Repository
public interface ModuelDescriptionRepository extends JpaRepository<ModuelDescription, Long> {
    Optional<ModuelDescription> findByModuleId(UUID moduleId);
    boolean existsByModuleId(UUID moduleId);
    void deleteByModuleId(UUID moduleId);
}
