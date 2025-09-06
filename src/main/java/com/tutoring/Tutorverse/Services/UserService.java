package com.tutoring.Tutorverse.Services;


import com.tutoring.Tutorverse.Dto.UserCreateDto;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.tutoring.Tutorverse.Repository.userRepository;
import java.util.UUID;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private userRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private  RoleService roleService;


    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public Optional<User> addUser(UserCreateDto request){
        if(userRepo.existsByEmail(request.getEmail())) {
            return userRepo.findByEmail(request.getEmail());
        }
        User newuser = new User();
        newuser.setEmail(request.getEmail());
        newuser.setName(request.getName());
        if(request.getPassword() != null){
            newuser.setPassword(encoder.encode(request.getPassword()));
        }
        newuser.setEmailVerified(request.isEmailVerified());
        newuser.setProviderid(request.getProviderId());
        newuser.setRole(roleService.findByName(request.getRole()));
        User savedUser = userRepo.save(newuser);
        return Optional.of(savedUser);
    }

    public boolean isTutor(UUID id){
        Optional<User> userOpt = userRepo.findById(id);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            return user.getRole() != null && "TUTOR".equalsIgnoreCase(user.getRole().getName());
        }
        return false;
    }
    public boolean isStudent(UUID id){
        Optional<User> userOpt = userRepo.findById(id);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            return user.getRole() != null && "STUDENT".equalsIgnoreCase(user.getRole().getName());
        }
        return false;
    }



}
