package com.tutoring.Tutorverse.Services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tutoring.Tutorverse.Dto.EnrollGetDto;
import com.tutoring.Tutorverse.Dto.ModuelsDto;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tutoring.Tutorverse.Dto.EnrollCreateDto;
import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Model.EnrollmentEntity;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import com.tutoring.Tutorverse.Repository.EnrollRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;
import com.tutoring.Tutorverse.Repository.StudentProfileRepository;
import com.tutoring.Tutorverse.Repository.EmailRepository;

@Service
public class EnrollmentService {
    @Autowired
    private EnrollRepository enrollRepository;
    @Autowired
    private StudentProfileRepository studentProfileRepository;
    @Autowired
    private ModulesRepository moduleRepository;
    @Autowired
    private userRepository userRepo;
    @Autowired
    private EmailRepository emailRepository;

    public String EnrollTOModule(EnrollCreateDto enrollCreateDto) {
        StudentEntity student = findStudentById(enrollCreateDto.getStudentId());
        ModuelsEntity module = findModuleById(enrollCreateDto.getModuleId());
        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setStudent(student);
        enrollment.setModule(module);
        enrollment.setPaid(false);
        enrollRepository.save(enrollment);
        return "Enrolled Successfully " + enrollment.getEnrolmentId();
    }

    public List<ModuelsDto> getEnrollmentByStudentId(UUID studentID){
        if(!isStudent(studentID)){
            throw new RuntimeException("User is not a student");
        }
        List<EnrollmentEntity> enrollments = enrollRepository.findByStudentStudentId(studentID);
        return enrollments.stream().map(this::convertToDto).toList();
    }

    private ModuelsDto convertToDto(EnrollmentEntity enrollmentEntity) {
        ModuelsEntity module = enrollmentEntity.getModule();
        return ModuelsDto.builder()
                .moduleId(module.getModuleId())
                .tutorId(module.getTutorId())
                .name(module.getName())
                .domain(module.getDomain() != null ? module.getDomain().getName() : null)
                .averageRatings(module.getAverageRatings())
                .fee(module.getFee())
                .duration(module.getDuration())
                .status(module.getStatus() != null ? module.getStatus().toString() : null)
                .build();
    }


    private ModuelsEntity findModuleById(UUID moduleId) {
       return moduleRepository.findById(moduleId).orElseThrow(() -> new RuntimeException("Module not found"));
    }
 
    private StudentEntity findStudentById(UUID studentId) {
        return studentProfileRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));
        
    }

    public boolean findIsPaidByStudentIdAndModuleId(UUID studentId, UUID moduleId) {
        return enrollRepository.findIsPaidByStudentIdAndModuleId(studentId, moduleId).orElseThrow(() -> new RuntimeException("Enrollment not found"));

    }

    public void unenrollFromModule(UUID enrollmentId) {
        enrollRepository.deleteById(enrollmentId);
    }
    public boolean isStudent(UUID id){
        Optional<User> userOpt = userRepo.findById(id);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            return user.getRole() != null && "STUDENT".equalsIgnoreCase(user.getRole().getName());
        }
        return false;
    }

    public List<String> getStudentEmailsByModuleId(UUID moduleId) {
        return emailRepository.findEmailsByModuleId(moduleId);
    }

    public UUID getEnrollmentId(UUID userId, UUID fromString) {
        EnrollmentEntity enrollment = enrollRepository.findByStudentStudentIdAndModuleModuleId(userId, fromString);
        return enrollment != null ? enrollment.getEnrolmentId() : null;
    }



}
