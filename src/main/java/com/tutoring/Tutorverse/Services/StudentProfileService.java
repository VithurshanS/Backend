
package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Dto.StudentProfileDto;
import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.StudentProfileRepository;
import com.tutoring.Tutorverse.Repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.management.RuntimeErrorException;


@Service
public class StudentProfileService {

	@Autowired
	private StudentProfileRepository studentRepository;

	@Autowired
	private userRepository userRepository;


	   private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public StudentEntity createStudentProfile(StudentProfileDto dto) {
		// Fetch the user by ID
		User user = userRepository.findById(dto.getStudentId())
			.orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getStudentId()));

		StudentEntity student = StudentEntity.builder()
				.user(user)
				.firstName(dto.getFirstName())
				.lastName(dto.getLastName())
				.address(dto.getAddress())
				.city(dto.getCity())
				.country(dto.getCountry())
				.birthday(dto.getBirthday())
				.imageUrl(dto.getImageUrl())
				.phoneNumber(dto.getPhoneNumber())
				.bio(dto.getBio())
				.build();
		return studentRepository.save(student);
	}

	public StudentEntity getStudentProfile(UUID id) {
		return studentRepository.findById(id).orElseThrow(() -> new RuntimeErrorException(null, "Student not found with id: " + id));
	}

	public StudentEntity updateStudentProfile(UUID id, StudentProfileDto dto) {
		StudentEntity student = getStudentProfile(id);
		if (student != null) {
			student.setFirstName(dto.getFirstName());
			student.setLastName(dto.getLastName());
			student.setAddress(dto.getAddress());
			student.setCity(dto.getCity());
			student.setCountry(dto.getCountry());
			student.setBirthday(dto.getBirthday());
			student.setImageUrl(dto.getImageUrl());
			student.setPhoneNumber(dto.getPhoneNumber());
			student.setBio(dto.getBio());
			return studentRepository.save(student);
		}
		return null;
	}

	public void deleteStudentProfile(UUID id) {
		studentRepository.deleteById(id);
	}


	public void changePassword(UUID id, String newPassword) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
		if (user != null && newPassword != user.getPassword()) {
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);
		}
	}

	public Integer getStudentCount() {
		return studentRepository.findAll().size();
	}

	public Integer getActiveStudentCount() {
		return studentRepository.findByIsActiveTrue().size();
	}

	public Integer getBannedStudentCount() {
		return studentRepository.findByIsActiveFalse().size();
	}

	public List<StudentEntity> getAllStudents() {
		return studentRepository.findAll();
	}

	public void banStudent(UUID id){
		StudentEntity student = studentRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
		student.setIsActive(false);
		studentRepository.save(student);
	}

	public Map<String, Object> lastMonthGrowth(){
		YearMonth currentMonth = YearMonth.now();
		YearMonth lastMonth = currentMonth.minusMonths(1);
		YearMonth previousMonth = currentMonth.minusMonths(2);

		LocalDateTime lastStart = lastMonth.atDay(1).atStartOfDay();
		LocalDateTime lastEnd = lastMonth.atEndOfMonth().atTime(23,59,59,999_999_999);

		LocalDateTime prevStart = previousMonth.atDay(1).atStartOfDay();
		LocalDateTime prevEnd = previousMonth.atEndOfMonth().atTime(23,59,59,999_999_999);

		long lastCount = studentRepository.countByCreatedAtBetween(lastStart, lastEnd);
		long prevCount = studentRepository.countByCreatedAtBetween(prevStart, prevEnd);

		double growthPercent;
		if (prevCount == 0) {
			growthPercent = lastCount > 0 ? 100.0 : 0.0;
		} else {
			growthPercent = ((double)(lastCount - prevCount) / (double)prevCount) * 100.0;
		}

		return Map.of(
			"lastMonth", lastMonth.toString(),
			"previousMonth", previousMonth.toString(),
			"lastMonthCount", lastCount,
			"previousMonthCount", prevCount,
			"growthPercent", growthPercent
		);
	}

	public void unbanStudent(UUID id){
		StudentEntity student = studentRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
		student.setIsActive(true);
		studentRepository.save(student);
	}

	

}
