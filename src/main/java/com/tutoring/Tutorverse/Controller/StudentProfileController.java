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
import java.util.HashMap;
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

	@GetMapping("/count")
	public ResponseEntity<?> getStudentCount() {
		try {
			Integer count = studentProfileService.getStudentCount();
			Integer activeCount = studentProfileService.getActiveStudentCount();
			Integer bannedCount = studentProfileService.getBannedStudentCount();
			// Map.of() does not allow null values; default nulls to 0 to prevent NPE / Whitelabel error
			int safeTotal = count == null ? 0 : count;
			int safeActive = activeCount == null ? 0 : activeCount;
			int safeBanned = bannedCount == null ? 0 : bannedCount;
			Map<String, Integer> payload = new HashMap<>();
			payload.put("total", safeTotal);
			payload.put("active", safeActive);
			payload.put("banned", safeBanned);
			return ResponseEntity.ok().body(payload);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving student count: " + e.getMessage());
		}
	}

	@GetMapping("/all")
	public ResponseEntity<?> getAllStudents() {
		try {
			return ResponseEntity.ok(studentProfileService.getAllStudents());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving students: " + e.getMessage());
		}
	}

	@PostMapping("/ban")
	public ResponseEntity<?> banStudent(@RequestParam UUID studentId) {
		try {
			studentProfileService.banStudent(studentId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error banning student: " + e.getMessage());
		}
	}
	
	@PostMapping("/unban")
	public ResponseEntity<?> unbanStudent(@RequestParam UUID studentId) {
		try {
			studentProfileService.unbanStudent(studentId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error unbanning student: " + e.getMessage());
		}
	}


	@GetMapping("/countall")
	public ResponseEntity<?> getTotalStudentCount() {
		try {
			Integer totalCount = studentProfileService.getStudentCount();
			Map<String, Integer> payload = new HashMap<>();
			payload.put("totalCount", totalCount);
			return ResponseEntity.ok().body(payload);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving total student count: " + e.getMessage());
		}
	}

	@GetMapping("/growthstudent/last-month")
	public ResponseEntity<?> getLastMonthGrowth() {
		try {
			Map<String, Object> growthData = studentProfileService.lastMonthGrowth();
			return ResponseEntity.ok().body(growthData);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving growth data: " + e.getMessage());
		}
	}
	

}
