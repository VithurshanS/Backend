package com.tutoring.Tutorverse.Controller;

import com.tutoring.Tutorverse.Dto.StudentProfileDto;
import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Services.StudentProfileService;
import com.tutoring.Tutorverse.Services.JwtServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/student-profile")
public class StudentProfileController {

	

	@Autowired
	private StudentProfileService studentProfileService;

	@Autowired
	private JwtServices jwtServices;

	@PostMapping
	public ResponseEntity<?> createStudentProfile(@RequestBody StudentProfileDto dto,
												  @RequestHeader(value = "Authorization", required = false) String authHeader) {
		try {
			if (authHeader == null || authHeader.isBlank()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
			}
			String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
			UUID userId = jwtServices.getUserIdFromJwtToken(token);
			dto.setStudentId(userId); // override any provided id
			StudentEntity createdStudent = studentProfileService.createStudentProfile(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
		}
	}


	@GetMapping("/me")
	public ResponseEntity<?> getStudentProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
		try {
			if (authHeader == null || authHeader.isBlank()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
			}
			String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
			UUID userId = jwtServices.getUserIdFromJwtToken(token);
			StudentEntity student = studentProfileService.getStudentProfile(userId);
			return ResponseEntity.ok(student);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
		}
	}

	@PutMapping
	public ResponseEntity<?> updateStudentProfile(@RequestBody StudentProfileDto dto,
												  @RequestHeader(value = "Authorization", required = false) String authHeader) {
		try {
			if (authHeader == null || authHeader.isBlank()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
			}
			String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
			UUID userId = jwtServices.getUserIdFromJwtToken(token);
			dto.setStudentId(userId);
			StudentEntity updatedStudent = studentProfileService.updateStudentProfile(userId, dto);
			return ResponseEntity.ok(updatedStudent);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
		}
	}

	@DeleteMapping
	public ResponseEntity<?> deleteStudentProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
		try {
			if (authHeader == null || authHeader.isBlank()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
			}
			String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
			UUID userId = jwtServices.getUserIdFromJwtToken(token);
			studentProfileService.deleteStudentProfile(userId);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
		}
	}

	@PutMapping("/change-password")
	 public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body,
														  @RequestHeader(value = "Authorization", required = false) String authHeader) {
		try {
			if (authHeader == null || authHeader.isBlank()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
			}
			String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
			UUID userId = jwtServices.getUserIdFromJwtToken(token);
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
