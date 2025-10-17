package com.tutoring.Tutorverse.Services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tutoring.Tutorverse.Dto.ModuelDescriptionDto;
import com.tutoring.Tutorverse.Model.ModuelDescription;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import com.tutoring.Tutorverse.Repository.ModuelDescriptionRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;

@Service
public class ModuelDescriptionService {

    private static final String DELIM = "#";

    @Autowired
    private ModuelDescriptionRepository descriptionRepo;

    @Autowired
    private ModulesRepository modulesRepository;

    public ModuelDescriptionDto create(UUID tutorId, ModuelDescriptionDto dto) {
        if (dto.getModuleId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "moduleId is required");

        ModuelsEntity module = modulesRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        // ownership check
        if (!module.getTutorId().equals(tutorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of module");
        }

        // upsert-like behavior: if exists, update; else create
        Optional<ModuelDescription> existingOpt = descriptionRepo.findByModuleId(dto.getModuleId());
        ModuelDescription entity = existingOpt.orElseGet(ModuelDescription::new);

        entity.setModuleId(dto.getModuleId());
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDomain() != null) entity.setDomain(dto.getDomain());
        if (dto.getPrice() != null) entity.setPrice(dto.getPrice());
        if (dto.getTutorName() != null) entity.setTutorName(dto.getTutorName());
        if (dto.getDescriptionPoints() != null && !dto.getDescriptionPoints().isEmpty()) {
            entity.setDescription(joinPoints(dto.getDescriptionPoints()));
        }

        ModuelDescription saved = descriptionRepo.save(entity);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ModuelDescriptionDto getByModuleId(UUID moduleId) {
        ModuelDescription entity = descriptionRepo.findByModuleId(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Description not found"));
        return toDto(entity);
    }

    @Transactional
    public ModuelDescriptionDto update(UUID tutorId, UUID moduleId, ModuelDescriptionDto dto) {
        ModuelsEntity module = modulesRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        if (!module.getTutorId().equals(tutorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of module");
        }

        ModuelDescription entity = descriptionRepo.findByModuleId(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Description not found"));

        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDomain() != null) entity.setDomain(dto.getDomain());
        if (dto.getPrice() != null) entity.setPrice(dto.getPrice());
        if (dto.getTutorName() != null) entity.setTutorName(dto.getTutorName());
        if (dto.getDescriptionPoints() != null) {
            entity.setDescription(joinPoints(dto.getDescriptionPoints()));
        }

        ModuelDescription saved = descriptionRepo.save(entity);
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID tutorId, UUID moduleId) {
        ModuelsEntity module = modulesRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        if (!module.getTutorId().equals(tutorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of module");
        }
        descriptionRepo.findByModuleId(moduleId).ifPresent(d -> descriptionRepo.deleteById(d.getId()));
    }

    private String joinPoints(List<String> points) {
        // Trim and filter blanks, then join with '#'
        List<String> cleaned = points.stream()
                .map(p -> p == null ? "" : p.trim())
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());
        return String.join(DELIM, cleaned);
    }

    private List<String> splitPoints(String description) {
        if (description == null || description.isBlank()) return List.of();
        return Arrays.stream(description.split("\\#"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private ModuelDescriptionDto toDto(ModuelDescription entity) {
        return ModuelDescriptionDto.builder()
                .id(entity.getId())
                .moduleId(entity.getModuleId())
                .name(entity.getName())
                .domain(entity.getDomain())
                .price(entity.getPrice())
                .tutorName(entity.getTutorName())
                .descriptionPoints(splitPoints(entity.getDescription()))
                .build();
    }

    public boolean exists(UUID moduleId) {
        return descriptionRepo.existsByModuleId(moduleId);
}

}
