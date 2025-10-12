package com.tutoring.Tutorverse.Admin.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tutoring.Tutorverse.Admin.Model.AdminProfileEntity;

import java.util.UUID;

public interface AdminProfileRepository extends JpaRepository<AdminProfileEntity, UUID> {
}
