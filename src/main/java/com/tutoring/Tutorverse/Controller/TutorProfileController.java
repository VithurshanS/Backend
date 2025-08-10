package com.tutoring.Tutorverse.Controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tutoring.Tutorverse.Dto.TutorProfileDto;
import com.tutoring.Tutorverse.Model.TutorEntity;
import com.tutoring.Tutorverse.Services.TutorProfileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/tutor-profile")
public class TutorProfileController {


    @Autowired
    private TutorProfileService tutorProfileService;

    @PostMapping
    public ResponseEntity<TutorEntity> createTutorProfile(@RequestBody TutorProfileDto dto) {
        TutorEntity createdProfile = tutorProfileService.createTutorProfile(dto);
        return ResponseEntity.ok(createdProfile);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<TutorEntity> getTutorProfile(@PathVariable UUID id) {
        TutorEntity tutorProfile = tutorProfileService.getTutorProfile(id);
        return ResponseEntity.ok(tutorProfile);
    }


    @PostMapping("/update/{id}")
    public ResponseEntity<TutorEntity> updateTutorProfile(@PathVariable UUID id, @RequestBody TutorProfileDto dto) {
        TutorEntity updatedProfile = tutorProfileService.updateTutorProfile(id, dto);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTutorProfile(@PathVariable UUID id) {
        tutorProfileService.deleteTutorProfile(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-password/{id}")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        tutorProfileService.changePassword(id, newPassword);
        return ResponseEntity.ok().build();
    }

}
