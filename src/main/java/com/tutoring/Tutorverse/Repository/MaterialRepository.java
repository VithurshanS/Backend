package com.tutoring.Tutorverse.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.tutoring.Tutorverse.Model.MaterialEntity;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, UUID> {
    @Query("SELECT m FROM MaterialEntity m WHERE m.module.moduleId = :moduleId")
    Collection<MaterialEntity> findMaterialByModuleId(@Param("moduleId") UUID moduleId);
}
