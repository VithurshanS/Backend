package com.tutoring.Tutorverse.Repository;



import com.tutoring.Tutorverse.Model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByOrderId(String orderId);
    @Query("SELECT COALESCE(SUM(p.amount), 0) " +
           "FROM PaymentEntity p " +
           "WHERE p.module.id = :moduleId AND p.status = :status")
    Double sumAmountByModuleIdAndStatus(@Param("moduleId") UUID moduleId,
                                        @Param("status") String status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) " +
           "FROM PaymentEntity p " +
           "WHERE p.student.id = :studentId AND p.status = :status")
    Double sumAmountByStudentIdAndStatus(@Param("studentId") UUID studentId,
                                         @Param("status") String status);
}
