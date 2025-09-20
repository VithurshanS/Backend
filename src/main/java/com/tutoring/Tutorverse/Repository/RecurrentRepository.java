package com.tutoring.Tutorverse.Repository;

import com.tutoring.Tutorverse.Model.RecurrentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecurrentRepository extends JpaRepository<RecurrentEntity, Integer> {
    
    // Find recurrent type by name
    Optional<RecurrentEntity> findByRecurrentType(String recurrentType);
    
    // Check if recurrent type exists
    boolean existsByRecurrentType(String recurrentType);
}
