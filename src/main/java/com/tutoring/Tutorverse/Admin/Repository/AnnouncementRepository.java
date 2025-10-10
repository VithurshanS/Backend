package com.tutoring.Tutorverse.Admin.Repository;

import com.tutoring.Tutorverse.Admin.Model.AnnouncementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, UUID> {
    List<AnnouncementEntity> findAllByIsActiveTrueOrderByCreatedAtDesc();
    List<AnnouncementEntity> findAllByOrderByCreatedAtDesc();
    List<AnnouncementEntity> findAllByAuthor(String author);
}
