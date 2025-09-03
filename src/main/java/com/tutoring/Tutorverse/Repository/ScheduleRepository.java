package com.tutoring.Tutorverse.Repository;

import com.tutoring.Tutorverse.Model.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, UUID> {
    
    // Find schedules by module ID
    List<ScheduleEntity> findByModule_ModuleId(UUID moduleId);
    
    // Find schedules by tutor ID (through module relationship)
    List<ScheduleEntity> findByModule_TutorId(UUID tutorId);
    
    // Find schedules by specific date
    List<ScheduleEntity> findByDate(LocalDate date);
    
    // Find schedules by date range
    List<ScheduleEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find schedules by week number (for recurring schedules)
    List<ScheduleEntity> findByWeekNumber(Integer weekNumber);
    
    // Find schedules by recurrent type
    List<ScheduleEntity> findByRecurrent_RecurrentType(String recurrentType);
    
    // Find upcoming schedules (date >= today)
    List<ScheduleEntity> findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate date);
}
