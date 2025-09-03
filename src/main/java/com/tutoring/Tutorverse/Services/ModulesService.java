package com.tutoring.Tutorverse.Services;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.tutoring.Tutorverse.Dto.ModuelsDto;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import com.tutoring.Tutorverse.Model.ModuelsEntity.ModuleStatus;
import com.tutoring.Tutorverse.Model.DomainEntity;
import com.tutoring.Tutorverse.Repository.ModulesRepository;  
import com.tutoring.Tutorverse.Repository.DomainRepository;


@Service
public class ModulesService {

    @Autowired
    public ModulesRepository modulesRepository;

    @Autowired
    private DomainRepository domainRepository;

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
        try {
            if (moduelsDto.getName() == null || moduelsDto.getName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Module name required");
            }
            if (moduelsDto.getFee() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fee required");
            }

            ModuelsEntity entity = new ModuelsEntity();
            entity.setTutorId(moduelsDto.getTutorId());
            entity.setName(moduelsDto.getName().trim());
            entity.setFee(moduelsDto.getFee());
            entity.setDuration(moduelsDto.getDuration());

            // Domain: resolve by name if provided, create if not exists
            if (moduelsDto.getDomain() != null && !moduelsDto.getDomain().isBlank()) {
                String domainName = moduelsDto.getDomain().trim();
                DomainEntity domain = domainRepository.findAll().stream()
                        .filter(d -> d.getName().equalsIgnoreCase(domainName))
                        .findFirst()
                        .orElseGet(() -> {
                            // Create new domain if not found
                            DomainEntity newDomain = DomainEntity.builder()
                                    .name(domainName)
                                    .build();
                            return domainRepository.save(newDomain);
                        });
                entity.setDomain(domain);
            }

            // Status mapping (optional)
            if (moduelsDto.getStatus() != null) {
                try {
                    entity.setStatus(ModuleStatus.valueOf(moduelsDto.getStatus()));
                } catch (IllegalArgumentException iae) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
                }
            } else {
                entity.setStatus(ModuleStatus.Draft); // default
            }

            modulesRepository.save(entity);
        } catch (ResponseStatusException rse) {
            throw rse; // rethrow to controller layer
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create module");
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

    public List<ModuelsDto> getModulesByDomainId(Integer id) {
        try {
            List<ModuelsDto> results = modulesRepository.findByDomain_DomainId(id).stream()
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