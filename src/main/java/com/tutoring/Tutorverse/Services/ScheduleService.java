package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Dto.ScheduleDto;
import com.tutoring.Tutorverse.Model.ScheduleEntity;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import com.tutoring.Tutorverse.Model.RecurrentEntity;
import com.tutoring.Tutorverse.Repository.ScheduleRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;
import com.tutoring.Tutorverse.Repository.RecurrentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ModulesRepository modulesRepository;

    @Autowired
    private RecurrentRepository recurrentRepository;

    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        try {
            // Validate input
            if (!scheduleDto.isValid()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid schedule data provided");
            }

            // Check if module exists
            Optional<ModuelsEntity> moduleOpt = modulesRepository.findById(scheduleDto.getModuleId());
            if (moduleOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
            }
            ModuelsEntity module = moduleOpt.get();

            // Create schedule entity
            ScheduleEntity schedule = ScheduleEntity.builder()
                    .module(module)
                    .date(scheduleDto.getDate())
                    .time(scheduleDto.getTime())
                    .duration(scheduleDto.getDuration())
                    .weekNumber(scheduleDto.getWeekNumber())
                    .build();

            // Handle recurrent type if provided
            if (scheduleDto.getRecurrentType() != null && !scheduleDto.getRecurrentType().isBlank()) {
                Optional<RecurrentEntity> recurrentOpt = recurrentRepository.findByRecurrentType(scheduleDto.getRecurrentType());
                if (recurrentOpt.isPresent()) {
                    schedule.setRecurrent(recurrentOpt.get());
                } else {
                    // Create new recurrent type if it doesn't exist
                    RecurrentEntity newRecurrent = RecurrentEntity.builder()
                            .recurrentType(scheduleDto.getRecurrentType())
                            .build();
                    schedule.setRecurrent(recurrentRepository.save(newRecurrent));
                }
            }

            // Save schedule - this will trigger the database clash detection
            ScheduleEntity savedSchedule = scheduleRepository.save(schedule);
            
            return convertToDto(savedSchedule);

        } catch (DataIntegrityViolationException e) {
            // Handle schedule clash exception from database trigger
            String message = e.getMostSpecificCause().getMessage();
            if (message != null && message.contains("Schedule clash detected")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Schedule conflict: This time slot overlaps with an existing schedule for the same tutor");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Database constraint violation");
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create schedule");
        }
    }

    public List<ScheduleDto> getAllSchedules() {
        try {
            return scheduleRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<ScheduleDto> getSchedulesByModuleId(UUID moduleId) {
        try {
            return scheduleRepository.findByModule_ModuleId(moduleId).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<ScheduleDto> getSchedulesByTutorId(UUID tutorId) {
        try {
            return scheduleRepository.findByModule_TutorId(tutorId).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void deleteSchedule(UUID scheduleId) {
        try {
            if (!scheduleRepository.existsById(scheduleId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found");
            }
            scheduleRepository.deleteById(scheduleId);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete schedule");
        }
    }

    public ScheduleDto updateSchedule(UUID scheduleId, ScheduleDto scheduleDto) {
        try {
            Optional<ScheduleEntity> existingOpt = scheduleRepository.findById(scheduleId);
            if (existingOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found");
            }

            ScheduleEntity existing = existingOpt.get();
            
            // Update fields
            if (scheduleDto.getDate() != null) existing.setDate(scheduleDto.getDate());
            if (scheduleDto.getTime() != null) existing.setTime(scheduleDto.getTime());
            if (scheduleDto.getDuration() != null) existing.setDuration(scheduleDto.getDuration());
            if (scheduleDto.getWeekNumber() != null) existing.setWeekNumber(scheduleDto.getWeekNumber());

            // Handle recurrent type update
            if (scheduleDto.getRecurrentType() != null) {
                Optional<RecurrentEntity> recurrentOpt = recurrentRepository.findByRecurrentType(scheduleDto.getRecurrentType());
                if (recurrentOpt.isPresent()) {
                    existing.setRecurrent(recurrentOpt.get());
                } else {
                    RecurrentEntity newRecurrent = RecurrentEntity.builder()
                            .recurrentType(scheduleDto.getRecurrentType())
                            .build();
                    existing.setRecurrent(recurrentRepository.save(newRecurrent));
                }
            }

            ScheduleEntity updated = scheduleRepository.save(existing);
            return convertToDto(updated);

        } catch (DataIntegrityViolationException e) {
            String message = e.getMostSpecificCause().getMessage();
            if (message != null && message.contains("Schedule clash detected")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Schedule conflict: This time slot overlaps with an existing schedule for the same tutor");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Database constraint violation");
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update schedule");
        }
    }

    private ScheduleDto convertToDto(ScheduleEntity entity) {
        return ScheduleDto.builder()
                .scheduleId(entity.getScheduleId())
                .moduleId(entity.getModule().getModuleId())
                .date(entity.getDate())
                .time(entity.getTime())
                .duration(entity.getDuration())
                .weekNumber(entity.getWeekNumber())
                .recurrentType(entity.getRecurrent() != null ? entity.getRecurrent().getRecurrentType() : null)
                .moduleName(entity.getModule().getName())
                .tutorName(entity.getModule().getTutor() != null ? 
                    entity.getModule().getTutor().getFirstName() + " " + entity.getModule().getTutor().getLastName() : null)
                .build();
    }
}
