package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Dto.MaterialDto;
import com.tutoring.Tutorverse.Model.MaterialEntity;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import com.tutoring.Tutorverse.Repository.MaterialRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterialService {

    @Autowired
    MaterialRepository materialRepository;

    @Autowired
    ModulesRepository modulesRepository;



    public List<MaterialDto> getAllMaterialsByModuleId(UUID moduleId) {
        try {
            return materialRepository.findMaterialByModuleId(moduleId).stream() // use .stream() here
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }



    private MaterialDto convertToDto(MaterialEntity entity) {
        return MaterialDto.builder()
                .material_id(entity.getMaterial_id())
                .moduleId(entity.getModule() != null ? entity.getModule().getModuleId() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .type(entity.getType())
                .url(entity.getUrl())
                .build();
    }

    public MaterialDto saveToDatabase(UUID module_id, String title, String description, String type, String url) {
        ModuelsEntity module = modulesRepository.findById(module_id).orElse(null);
        if (module == null) {
            throw new RuntimeException("Module not found with id: " + module_id);
        }

        MaterialEntity material = MaterialEntity.builder()
                .module(module)
                .title(title)
                .description(description)
                .type(type)
                .url(url)
                .build();

        MaterialEntity savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }
}
