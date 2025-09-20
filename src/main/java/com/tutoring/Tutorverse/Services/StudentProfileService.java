
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

}
