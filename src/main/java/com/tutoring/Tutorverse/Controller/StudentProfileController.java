package com.tutoring.Tutorverse.Controller;

import com.tutoring.Tutorverse.Dto.StudentProfileDto;
import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Services.StudentProfileService;
import com.tutoring.Tutorverse.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/student-profile")
public class StudentProfileController {

	

	@Autowired
	private StudentProfileService studentProfileService;

	@Autowired
	private UserService userService;

	@PostMapping
	public ResponseEntity<?> createStudentProfile(@RequestBody StudentProfileDto dto, HttpServletRequest req) {
		try {
			UUID userId = userService.getUserIdFromRequest(req);
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
			}
			dto.setStudentId(userId); // override any provided id
			StudentEntity createdStudent = studentProfileService.createStudentProfile(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
		}
	}


	@GetMapping("/me")
	public ResponseEntity<?> getStudentProfile(HttpServletRequest req) {
		try {
			UUID userId = userService.getUserIdFromRequest(req);
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
			}
			StudentEntity student = studentProfileService.getStudentProfile(userId);
			return ResponseEntity.ok(student);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
		}
	}

	@PutMapping
	public ResponseEntity<?> updateStudentProfile(@RequestBody StudentProfileDto dto, HttpServletRequest req) {
		try {
			UUID userId = userService.getUserIdFromRequest(req);
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
			}
			dto.setStudentId(userId);
			StudentEntity updatedStudent = studentProfileService.updateStudentProfile(userId, dto);
			return ResponseEntity.ok(updatedStudent);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
		}
	}

	@DeleteMapping
	public ResponseEntity<?> deleteStudentProfile(HttpServletRequest req) {
		try {
			UUID userId = userService.getUserIdFromRequest(req);
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
			}
			studentProfileService.deleteStudentProfile(userId);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
		}
	}

	@PutMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, HttpServletRequest req) {
		try {
			UUID userId = userService.getUserIdFromRequest(req);
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
			}
			String newPassword = body.get("newPassword");
			if (newPassword == null || newPassword.isBlank()) {
				return ResponseEntity.badRequest().body("newPassword is required");
			}
			studentProfileService.changePassword(userId, newPassword);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
		}
	}
}
