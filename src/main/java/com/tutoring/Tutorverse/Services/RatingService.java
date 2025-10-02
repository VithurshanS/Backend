package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Dto.RatingCreateDto;
import com.tutoring.Tutorverse.Dto.RatingGetDto;
import com.tutoring.Tutorverse.Model.EnrollmentEntity;
import com.tutoring.Tutorverse.Model.RatingEntity;
import com.tutoring.Tutorverse.Repository.EnrollRepository;
import com.tutoring.Tutorverse.Repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private EnrollRepository enrollRepository;

    @Transactional
    public String createRating(RatingCreateDto ratingCreateDto) {
        // Check if enrollment exists
        EnrollmentEntity enrollment = enrollRepository.findById(ratingCreateDto.getEnrolmentId())
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + ratingCreateDto.getEnrolmentId()));

        // Check if rating already exists for this enrollment
        if (ratingRepository.existsByEnrolmentId(ratingCreateDto.getEnrolmentId())) {
            throw new RuntimeException("Rating already exists for this enrollment");
        }

        UUID moduleId = enrollment.getModule() != null ? enrollment.getModule().getModuleId() : null;
        if(moduleId == null){
            throw new RuntimeException("Associated module not found for this enrollment");
        }

        // Create new rating
    // With @MapsId we do NOT manually set the primary key; it comes from the Enrollment entity
    RatingEntity rating = RatingEntity.builder()
        .enrollment(enrollment)
        .rating(ratingCreateDto.getRating())
        .feedback(ratingCreateDto.getFeedback())
        .studentName(ratingCreateDto.getStudentName())
        .moduleId(moduleId)
        .createdAt(Instant.now())
        .build();

        RatingEntity savedRating = ratingRepository.save(rating);
        // TODO: Optionally trigger module average rating recalculation here in future
        return "Rating created successfully with ID: " + savedRating.getEnrolmentId();
    }

    public RatingGetDto getRatingByEnrollmentId(UUID enrolmentId) {
        RatingEntity rating = ratingRepository.findByEnrolmentId(enrolmentId)
                .orElseThrow(() -> new RuntimeException("Rating not found for enrollment ID: " + enrolmentId));

        return convertToGetDto(rating);
    }

    public List<RatingGetDto> getRatingsByModuleId(UUID moduleId) {
        List<RatingEntity> ratings = ratingRepository.findByModuleId(moduleId);
        return ratings.stream()
                .map(this::convertToGetDto)
                .toList();
    }

    public List<RatingGetDto> getRatingsByStudentId(UUID studentId) {
        List<RatingEntity> ratings = ratingRepository.findByStudentId(studentId);
        return ratings.stream()
                .map(this::convertToGetDto)
                .toList();
    }

    public boolean hasRatingForEnrollment(UUID enrolmentId) {
        return ratingRepository.existsByEnrolmentId(enrolmentId);
    }

    public void deleteRating(UUID enrolmentId) {
        if (!ratingRepository.existsByEnrolmentId(enrolmentId)) {
            throw new RuntimeException("Rating not found for enrollment ID: " + enrolmentId);
        }
        ratingRepository.deleteById(enrolmentId);
    }

    private RatingGetDto convertToGetDto(RatingEntity rating) {
        return RatingGetDto.builder()
                .enrolmentId(rating.getEnrolmentId())
                .rating(rating.getRating())
                .feedback(rating.getFeedback())
                .studentName(rating.getStudentName())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
