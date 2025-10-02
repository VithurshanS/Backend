package com.tutoring.Tutorverse.Repository;

import com.tutoring.Tutorverse.Model.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
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

    // Call the find_matching_schedule database function
    @Query(value = "SELECT find_matching_schedule(:reqDate, :reqTime, :moduleId)", nativeQuery = true)
    UUID findMatchingSchedule(@Param("reqDate") LocalDate reqDate,
                             @Param("reqTime") LocalTime reqTime,
                             @Param("moduleId") UUID moduleId);

    // Native query for get_upcoming_schedules function (with active as boolean, correct order)
    @Query(value = "SELECT * FROM get_upcoming_schedules(:fromDate, :fromTime, :modId, :tutorId, :limitCount)", nativeQuery = true)
    List<Object[]> getUpcomingSchedules(
        @Param("fromDate") LocalDate fromDate,
        @Param("fromTime") LocalTime fromTime,
        @Param("modId") UUID modId,
        @Param("tutorId") UUID tutorId,
        @Param("limitCount") Integer limitCount
    );
}
