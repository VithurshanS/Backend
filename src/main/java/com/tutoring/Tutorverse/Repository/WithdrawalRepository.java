package com.tutoring.Tutorverse.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tutoring.Tutorverse.Model.WithdrawalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface WithdrawalRepository extends JpaRepository<WithdrawalEntity, UUID> {
    Page<WithdrawalEntity> findByTutorId(UUID tutorId, Pageable pageable);

    @Query("SELECT SUM(w.amount) FROM WithdrawalEntity w WHERE w.status = 'PENDING'")
    Double getTotalPendingAmount();

    @Query("SELECT SUM(w.amount) FROM WithdrawalEntity w WHERE w.status = 'APPROVED'")
    Double getTotalApprovedAmount();

    @Query("SELECT COUNT(w) FROM WithdrawalEntity w WHERE w.status = 'PENDING'")
    Long getPendingCount();
}
