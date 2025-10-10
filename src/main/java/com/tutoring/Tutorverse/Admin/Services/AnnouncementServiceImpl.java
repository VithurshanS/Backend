package com.tutoring.Tutorverse.Admin.Services;

import com.tutoring.Tutorverse.Admin.Dto.AnnouncementCreateDto;
import com.tutoring.Tutorverse.Admin.Dto.AnnouncementGetDto;
import com.tutoring.Tutorverse.Admin.Model.AnnouncementEntity;
import com.tutoring.Tutorverse.Admin.Repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.tutoring.Tutorverse.Admin.Dto.AnnouncementUpdateDto;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    private AnnouncementGetDto toDto(AnnouncementEntity e) {
        AnnouncementGetDto dto = new AnnouncementGetDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setContent(e.getContent());
        dto.setAuthor(e.getAuthor());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setActive(e.isActive());
        return dto;
    }

    @Override
    public AnnouncementGetDto create(AnnouncementCreateDto dto, String author) {
    AnnouncementEntity entity = new AnnouncementEntity();
    entity.setId(UUID.randomUUID());
    entity.setTitle(dto.getTitle());
    entity.setContent(dto.getContent());
    entity.setAuthor(author);
    entity.setCreatedAt(LocalDateTime.now());
    entity.setActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        AnnouncementEntity saved = announcementRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public AnnouncementGetDto getById(UUID id) {
        AnnouncementEntity entity = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found: " + id));
        return toDto(entity);
    }

    @Override
    public List<AnnouncementGetDto> list(Boolean onlyActive) {
        List<AnnouncementEntity> entities = Boolean.TRUE.equals(onlyActive)
                ? announcementRepository.findAllByIsActiveTrueOrderByCreatedAtDesc()
                : announcementRepository.findAllByOrderByCreatedAtDesc();
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public AnnouncementGetDto update(UUID id, AnnouncementUpdateDto dto) {
        AnnouncementEntity entity = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found: " + id));
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getContent() != null) entity.setContent(dto.getContent());
        if (dto.getIsActive() != null) entity.setActive(dto.getIsActive());
        AnnouncementEntity saved = announcementRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public void delete(UUID id) {
        announcementRepository.deleteById(id);
    }

    @Override
    public Object getByAuthor(String author) {
        List<AnnouncementEntity> entities = announcementRepository.findAllByAuthor(author);
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
