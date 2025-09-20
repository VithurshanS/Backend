package com.tutoring.Tutorverse.Repository;



import com.tutoring.Tutorverse.Model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByOrderId(String orderId);
}
