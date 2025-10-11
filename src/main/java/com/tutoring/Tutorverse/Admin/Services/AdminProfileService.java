package com.tutoring.Tutorverse.Admin.Services;

import com.tutoring.Tutorverse.Admin.Dto.AdminProfileDto;
import com.tutoring.Tutorverse.Admin.Model.AdminProfileEntity;
import com.tutoring.Tutorverse.Admin.Repository.AdminProfileRepository;
import com.tutoring.Tutorverse.Model.StudentEntity;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminProfileService {

    @Autowired
    private AdminProfileRepository adminRepo;

    @Autowired
    private userRepository userRepo;

    public AdminProfileEntity createOrUpdateAdminProfile(AdminProfileDto dto) {
        User user = userRepo.findById(dto.getAdminId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getAdminId()));

    // IMPORTANT: Do NOT set adminId manually for a new entity when using @MapsId.
    // Leaving adminId null lets JPA treat it as new and copy the PK from the associated User.
    // If we pre-set the ID, Spring Data assumes it's an existing row and issues an UPDATE.
    // When the row doesn't exist yet, Hibernate reports: "Row was updated or deleted by another transaction".
    AdminProfileEntity entity = adminRepo.findById(dto.getAdminId()).orElse(
        AdminProfileEntity.builder().user(user).build()
    );

        entity.setFullName(dto.getFullName());
        entity.setEmail(user.getEmail()); // enforce from user entity
        entity.setContactNumber(dto.getContactNumber());
        entity.setBio(dto.getBio());
        entity.setImageUrl(dto.getImageUrl());
        return adminRepo.save(entity);
    }

    public AdminProfileEntity getAdminProfile(UUID id) {
        return adminRepo.findById(id).orElseThrow(() -> new RuntimeException("Admin profile not found with id: " + id));
    }

    public void deleteAdminProfile(UUID id) {
        adminRepo.deleteById(id);
    }

    public void checkAdminExists(UUID id) {
        if (!adminRepo.existsById(id)) {
            throw new RuntimeException("Admin profile not found with id: " + id);
        }

    }

    public void changePassword(UUID id, String newPassword) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setPassword(newPassword); // In real applications, hash the password before saving
        userRepo.save(user);
    }

    public List<AdminProfileEntity> getAllAdminProfiles() {
        return adminRepo.findAll();
    }

    public String getAdmintImageUrl(UUID id) {
		AdminProfileEntity admin = adminRepo.findById(id)
			.orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));
		return admin.getImageUrl();
	}
}
