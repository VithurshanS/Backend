package com.tutoring.Tutorverse.Services;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.tutoring.Tutorverse.Dto.TutorProfileDto;
import com.tutoring.Tutorverse.Model.TutorEntity;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.TutorProfileRepository;
import com.tutoring.Tutorverse.Repository.userRepository;


@Service
public class TutorProfileService {

    @Autowired
    private TutorProfileRepository tutorRepository;

    @Autowired
	private userRepository userRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public TutorEntity createTutorProfile(TutorProfileDto dto) {

        User user = userRepository.findById(dto.getTutorId())
			.orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getTutorId()));

        TutorEntity tutor = TutorEntity.builder()
                .user(user)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNo(dto.getPhoneNo())
                .gender(dto.getGender())
                .dob(dto.getDob())
                .portfolio(dto.getPortfolio())
                .bio(dto.getBio())
                .image(dto.getImage())
                .address(dto.getAddress())
                .city(dto.getCity())
                .country(dto.getCountry())
                .build();
        return tutorRepository.save(tutor);
    }


    public TutorEntity getTutorProfile(UUID tutorId) {
        return tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor profile not found"));
    }


    public TutorEntity updateTutorProfile(UUID tutorId, TutorProfileDto dto) {
        TutorEntity existingProfile = getTutorProfile(tutorId);
        existingProfile.setFirstName(dto.getFirstName());
        existingProfile.setLastName(dto.getLastName());
        existingProfile.setPhoneNo(dto.getPhoneNo());
        existingProfile.setGender(dto.getGender());
        existingProfile.setDob(dto.getDob());
        existingProfile.setPortfolio(dto.getPortfolio());
        existingProfile.setBio(dto.getBio());
        existingProfile.setImage(dto.getImage());
        existingProfile.setAddress(dto.getAddress());
        existingProfile.setCity(dto.getCity());
        existingProfile.setCountry(dto.getCountry());
        return tutorRepository.save(existingProfile);
    }


    public void deleteTutorProfile(UUID tutorId) {
        TutorEntity existingProfile = getTutorProfile(tutorId);
        if(existingProfile == null){
            throw new RuntimeException("Tutor profile not found");
        }
        tutorRepository.delete(existingProfile);
    }

    public void changePassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        // Optional: Check if the new password is the same as the old one
        if (!passwordEncoder.matches(newPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }

    public TutorProfileDto convertToDto(TutorEntity tutor) {
        return TutorProfileDto.builder()
                .tutorId(tutor.getTutorId())
                .firstName(tutor.getFirstName())
                .lastName(tutor.getLastName())
                .phoneNo(tutor.getPhoneNo())
                .gender(tutor.getGender())
                .dob(tutor.getDob())
                .portfolio(tutor.getPortfolio())
                .bio(tutor.getBio())
                .image(tutor.getImage())
                .address(tutor.getAddress())
                .city(tutor.getCity())
                .country(tutor.getCountry())
                .build();
    }

    public List<TutorProfileDto> searchTutorProfiles(String query) {
        return tutorRepository.findByFirstNameContainingIgnoreCase(query).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public List<TutorEntity> getAllTutorProfiles() {
        return tutorRepository.findAll();
    }


}
