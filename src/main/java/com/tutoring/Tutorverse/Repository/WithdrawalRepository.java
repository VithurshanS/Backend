package com.tutoring.Tutorverse.Repository;

import com.tutoring.Tutorverse.Model.WithdrawalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WithdrawalRepository extends JpaRepository<WithdrawalEntity, UUID> {
    List<WithdrawalEntity> findByTutorId(UUID tutorId);
}