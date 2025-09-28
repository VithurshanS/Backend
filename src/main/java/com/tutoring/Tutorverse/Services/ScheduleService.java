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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ScheduleService {

    // Response type for upcoming session
    public static class UpcomingSessionResponse {
        public UUID schedule_id;
        public UUID module_id;
        public String tutor;
        public String course;
        public String Date;
        public String time;
        public Integer duration;
        public Boolean active;

        public UpcomingSessionResponse(UUID schedule_id, UUID module_id, String tutor, String course,String Date, String time, Integer duration, Boolean active) {
            this.schedule_id = schedule_id;
            this.module_id = module_id;
            this.tutor = tutor;
            this.course = course;
            this.Date = Date;
            this.time = time;
            this.duration = duration;
            this.active = active;
        }
    }

    // By module
    public List<UpcomingSessionResponse> getUpcomingSessionsByModule(LocalDate date, LocalTime time, UUID moduleId) {
        List<Object[]> rows = scheduleRepository.getUpcomingSchedules(date, time, moduleId, null, 10);
        List<UpcomingSessionResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new UpcomingSessionResponse(
                (UUID) row[0], // schedule_id
                (UUID) row[1], // module_id
                row[8] != null ? row[8].toString() : null, // tutor_name
                row[7] != null ? row[7].toString() : null, // course/module_name
                row[2] != null ? row[2].toString() : null, // Date
                row[4] != null ? row[4].toString() : null, // time
                row[5] != null ? ((Number) row[5]).intValue() : null, // duration
                row[3] != null ? (Boolean) row[3] : null // active
            ));
        }
        return result;
    }

    // By tutor (fetch all modules for tutor, then aggregate)
    public List<UpcomingSessionResponse> getUpcomingSessionsByTutor(LocalDate date, LocalTime time, UUID tutorId) {
        List<Object[]> rows = scheduleRepository.getUpcomingSchedules(date, time, null, tutorId, 10);
        List<UpcomingSessionResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new UpcomingSessionResponse(
                (UUID) row[0], // schedule_id
                (UUID) row[1], // module_id
                row[8] != null ? row[8].toString() : null, // tutor_name
                row[7] != null ? row[7].toString() : null, // course/module_name
                row[2] != null ? row[2].toString() : null,
                row[4] != null ? row[4].toString() : null, // time
                row[5] != null ? ((Number) row[5]).intValue() : null, // duration
                row[3] != null ? (Boolean) row[3] : null // active
            ));
        }
        return result;
    }

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

    public UUID findMatchingSchedule(LocalDate reqDate, LocalTime reqTime, UUID moduleId) {
        try {
            return scheduleRepository.findMatchingSchedule(reqDate, reqTime, moduleId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error calling find_matching_schedule function: " + e.getMessage());
        }
    }

    // Test method to call the function with your specified parameters
    public UUID testFindMatchingSchedule() {
        LocalDate reqDate = LocalDate.of(2025, 9, 10);
        LocalTime reqTime = LocalTime.of(10, 30, 0);
        UUID moduleId = UUID.fromString("6082f12a-2859-4ae5-93df-920ff6804fcf");

        return findMatchingSchedule(reqDate, reqTime, moduleId);
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
