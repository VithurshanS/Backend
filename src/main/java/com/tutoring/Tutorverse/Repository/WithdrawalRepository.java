package com.tutoring.Tutorverse.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tutoring.Tutorverse.Model.WithdrawalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WithdrawalRepository extends JpaRepository<WithdrawalEntity, UUID> {
    Page<WithdrawalEntity> findByTutorId(UUID tutorId, Pageable pageable);
}
