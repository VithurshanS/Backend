package com.tutoring.Tutorverse.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tutoring.Tutorverse.Model.Report;


public interface ReportRepository extends JpaRepository<Report, UUID> {
	
}


