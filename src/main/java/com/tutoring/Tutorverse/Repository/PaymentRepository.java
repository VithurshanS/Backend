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
           "WHERE p.module.moduleId = :moduleId AND p.status = :status")
    Double sumAmountByModuleIdAndStatus(@Param("moduleId") UUID moduleId,
                                        @Param("status") String status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) " +
           "FROM PaymentEntity p " +
           "WHERE p.student.studentId = :studentId AND p.status = :status")
    Double sumAmountByStudentIdAndStatus(@Param("studentId") UUID studentId,
                                         @Param("status") String status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.status = :status")
    Double sumAmountByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.status = :status AND p.createdAt BETWEEN :start AND :end")
    Double sumAmountByStatusAndCreatedAtBetween(@Param("status") String status,
                                                @Param("start") java.time.LocalDateTime start,
                                                @Param("end") java.time.LocalDateTime end);

    @Query("SELECT FUNCTION('to_char', p.createdAt, 'YYYY-MM') as ym, COALESCE(SUM(p.amount),0) as total " +
           "FROM PaymentEntity p WHERE p.status = :status AND p.createdAt >= :fromDate " +
           "GROUP BY FUNCTION('to_char', p.createdAt, 'YYYY-MM') ORDER BY ym")
    java.util.List<Object[]> sumAmountByMonthSince(@Param("status") String status,
                                                   @Param("fromDate") java.time.LocalDateTime fromDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) " +
            "FROM PaymentEntity p " +
            "JOIN p.module m " +
            "WHERE m.tutorId = :tutorId " +
            "AND p.status = 'SUCCESS'")
    Double findTotalEarningsByTutorId(@Param("tutorId") UUID tutorId);
}
