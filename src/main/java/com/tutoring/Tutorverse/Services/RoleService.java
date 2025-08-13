package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Model.Role;
import com.tutoring.Tutorverse.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        // Initialize default roles if they don't exist
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role("ADMIN");
            roleRepository.save(adminRole);
        }
        
        if (!roleRepository.existsByName("TUTOR")) {
            Role tutorRole = new Role("TUTOR");
            roleRepository.save(tutorRole);
        }
        
        if (!roleRepository.existsByName("STUDENT")) {
            Role studentRole = new Role("STUDENT");
            roleRepository.save(studentRole);
        }
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name.toUpperCase()).orElse(null);
    }

    public Role getDefaultRole() {
        Role studentRole = findByName("STUDENT");
        if (studentRole == null) {
            // Create STUDENT role if it doesn't exist
            studentRole = new Role("STUDENT");
            studentRole = roleRepository.save(studentRole);
        }
        return studentRole;
    }

    public Role getRoleByEnum(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return getDefaultRole();
        }
        
        String upperCaseName = roleName.toUpperCase();
        Role role = findByName(upperCaseName);
        
        if (role != null) {
            return role;
        }

        // If role doesn't exist, try to get default role
        Role defaultRole = getDefaultRole();
        if (defaultRole != null) {
            return defaultRole;
        }

        // If even default role doesn't exist, create and return STUDENT role
        Role studentRole = new Role("STUDENT");
        return roleRepository.save(studentRole);
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }
}
