package com.tutoring.Tutorverse.Services;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tutoring.Tutorverse.Dto.ModuelsDto;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import com.tutoring.Tutorverse.Repository.ModulesRepository;  


@Service
public class ModulesService {

    @Autowired
    public ModulesRepository modulesRepository;

    public List<ModuelsDto> getAllModules() {
        try{
            List<ModuelsDto> entities = modulesRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return entities;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private ModuelsDto convertToDto(ModuelsEntity entity) {
        return ModuelsDto.builder()
                .moduleId(entity.getModuleId())
                .tutorId(entity.getTutorId())
                .name(entity.getName())
                .domain(entity.getDomain() != null ? entity.getDomain().getName() : null)
                .averageRatings(entity.getAverageRatings())
                .fee(entity.getFee())
                .duration(entity.getDuration())
                .status(entity.getStatus() != null ? entity.getStatus().toString() : null)
                .build();
    }

    public void createModule(ModuelsDto moduelsDto) {
        try{
            ModuelsEntity moduelsEntity = new ModuelsEntity();
            moduelsEntity.setTutorId(moduelsDto.getTutorId());
            moduelsEntity.setName(moduelsDto.getName());
            moduelsEntity.setFee(moduelsDto.getFee());
            moduelsEntity.setDuration(moduelsDto.getDuration());
            modulesRepository.save(moduelsEntity);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public void deleteModule(UUID moduleId) {
        try {
            modulesRepository.deleteById(moduleId);
        } catch (Exception e) {
            System.out.println("Error deleting module: " + e.getMessage());
        }
    }

    public List<ModuelsDto> searchModules(String query) {
        try {
            List<ModuelsDto> results = modulesRepository.findByNameContainingIgnoreCase(query).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}

//