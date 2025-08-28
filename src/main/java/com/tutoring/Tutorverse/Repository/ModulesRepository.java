package com.tutoring.Tutorverse.Repository;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tutoring.Tutorverse.Model.ModuelsEntity;

public interface ModulesRepository extends JpaRepository<ModuelsEntity, UUID> {

    Collection<ModuelsEntity> findByNameContainingIgnoreCase(String query);

}
