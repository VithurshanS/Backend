package com.tutoring.Tutorverse.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ModulesRepository extends JpaRepository<ModuelsEntity, UUID> {

    Collection<ModuelsEntity> findByNameContainingIgnoreCase(String query);
    Collection<ModuelsEntity> findByDomain_DomainId(Integer domainId);
    Boolean existsByTutorIdAndModuleId(UUID tutorId, UUID moduleId);
    Collection<ModuelsEntity> findByTutor_TutorId(UUID tutorId);
    long countByCreatedAtBetween(LocalDateTime lastStart, LocalDateTime lastEnd);
    long count();
    long countByStatus(ModuelsEntity.ModuleStatus status);
    Optional<ModuelsEntity> findByModuleId(UUID moduleId);



}
