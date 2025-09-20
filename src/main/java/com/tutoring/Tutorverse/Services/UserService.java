package com.tutoring.Tutorverse.Services;


import com.tutoring.Tutorverse.Dto.UserCreateDto;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.tutoring.Tutorverse.Repository.userRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private userRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtServices jwtServices;


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

    /**
     * Extracts the user ID from the JWT token found in the request cookies
     * @param req HttpServletRequest containing the cookies
     * @return UUID of the user if valid token is found, null otherwise
     */
    public UUID getUserIdFromRequest(HttpServletRequest req) {
        try {
            if (req.getCookies() != null) {
                for (Cookie cookie : req.getCookies()) {
                    if ("jwt_token".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        if (token != null && jwtServices.validateJwtToken(token)) {
                            return jwtServices.getUserIdFromJwtToken(token);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting user ID from request: " + e.getMessage());
        }
        return null;
    }

}
