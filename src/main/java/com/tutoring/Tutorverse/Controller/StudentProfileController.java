package com.tutoring.Tutorverse.Controller;

import com.tutoring.Tutorverse.Dto.StudentProfileDto;
import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Services.StudentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/student-profile")
public class StudentProfileController {

	

	@Autowired
	private StudentProfileService studentProfileService;

	@PostMapping
	public ResponseEntity<StudentEntity> createStudentProfile(@RequestBody StudentProfileDto dto) {
		StudentEntity createdStudent = studentProfileService.createStudentProfile(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
	}


	@GetMapping("/{id}")
	public ResponseEntity<StudentEntity> getStudentProfile(@PathVariable UUID id) {
		StudentEntity student = studentProfileService.getStudentProfile(id);
		return ResponseEntity.ok(student);
	}

	@PutMapping("/{id}")
	public ResponseEntity<StudentEntity> updateStudentProfile(@PathVariable UUID id, @RequestBody StudentProfileDto dto) {
		StudentEntity updatedStudent = studentProfileService.updateStudentProfile(id, dto);
		return ResponseEntity.ok(updatedStudent);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteStudentProfile(@PathVariable UUID id) {
		studentProfileService.deleteStudentProfile(id);
		return ResponseEntity.noContent().build();
	}

   @PutMapping("/change-password/{id}")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        studentProfileService.changePassword(id, newPassword);
        return ResponseEntity.ok().build();
    }
}
