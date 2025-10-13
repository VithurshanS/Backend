package com.tutoring.Tutorverse.Admin.Services;

import com.tutoring.Tutorverse.Admin.Dto.AnnouncementCreateDto;
import com.tutoring.Tutorverse.Admin.Dto.AnnouncementGetDto;
import com.tutoring.Tutorverse.Admin.Dto.AnnouncementUpdateDto;

import java.util.List;
import java.util.UUID;

public interface AnnouncementService {
    AnnouncementGetDto create(AnnouncementCreateDto dto, String author);
    AnnouncementGetDto getById(UUID id);
    List<AnnouncementGetDto> list(Boolean onlyActive);
    AnnouncementGetDto update(UUID id, AnnouncementUpdateDto dto);
    void delete(UUID id);
    Object getByAuthor(String author);
}
