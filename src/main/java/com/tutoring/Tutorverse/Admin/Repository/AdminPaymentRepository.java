package com.tutoring.Tutorverse.Admin.Repository;

import com.tutoring.Tutorverse.Model.PaymentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminPaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    @Query("SELECT COALESCE(SUM(p.amount * 0.1), 0) FROM PaymentEntity p WHERE p.status = 'SUCCESS'")
    double getTotalPlatformRevenue();
}