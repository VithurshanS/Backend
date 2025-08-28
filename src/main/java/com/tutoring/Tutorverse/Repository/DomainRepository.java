package com.tutoring.Tutorverse.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tutoring.Tutorverse.Model.DomainEntity;

public interface DomainRepository extends JpaRepository<DomainEntity, Integer> {

}
