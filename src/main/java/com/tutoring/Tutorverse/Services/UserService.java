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
        newuser.setFirstName(request.getFirstName());
        newuser.setLastName(request.getLastName());
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
     * Extracts the user ID from the JWT token in the request cookies
     * @param req HttpServletRequest containing the cookies
     * @return UUID of the user if valid token is found, null otherwise
     */
    public UUID getUserIdFromRequest(HttpServletRequest req) {
        try {
            System.out.println("=== UserService.getUserIdFromRequest DEBUG ===");
            System.out.println("Request URI: " + req.getRequestURI());
            
            if (req.getCookies() != null) {
                System.out.println("Total cookies found: " + req.getCookies().length);
                for (Cookie cookie : req.getCookies()) {
                    System.out.println("Cookie: " + cookie.getName() + " = " + 
                        (cookie.getValue() != null ? cookie.getValue().substring(0, Math.min(30, cookie.getValue().length())) + "..." : "null"));
                    
                    if ("jwt_token".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        System.out.println("Found jwt_token cookie, validating...");
                        
                        if (token != null && jwtServices.validateJwtToken(token)) {
                            UUID userId = jwtServices.getUserIdFromJwtToken(token);
                            System.out.println("Token validation successful, user ID: " + userId);
                            return userId;
                        } else {
                            System.out.println("Token validation failed - token: " + (token != null ? "present" : "null"));
                        }
                    }
                }
            } else {
                System.out.println("No cookies found in request");
            }
        } catch (Exception e) {
            System.err.println("Error extracting user ID from request: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("getUserIdFromRequest returning null");
        return null;
    }

    /**
     * Extracts the JWT token from the request cookies
     * @param req HttpServletRequest containing the cookies
     * @return String token if found and valid, null otherwise
     */
    public String getTokenFromRequest(HttpServletRequest req) {
        try {
            if (req.getCookies() != null) {
                for (Cookie cookie : req.getCookies()) {
                    if ("jwt_token".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        if (token != null && jwtServices.validateJwtToken(token)) {
                            return token;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting token from request: " + e.getMessage());
        }
        return null;
    }

    /**
     * Validates if the current request has a valid user token and checks if user has required role
     * @param req HttpServletRequest containing the cookies
     * @param requiredRole Required role name (e.g., "TUTOR", "STUDENT", "ADMIN")
     * @return true if user is authenticated and has required role, false otherwise
     */
    public boolean hasRole(HttpServletRequest req, String requiredRole) {
        UUID userId = getUserIdFromRequest(req);
        if (userId == null) return false;
        
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getRole() != null && requiredRole.equalsIgnoreCase(user.getRole().getName());
        }
        return false;
    }

    /**
     * Gets the full User object from the request cookies
     * @param req HttpServletRequest containing the cookies
     * @return User object if valid token is found, null otherwise
     */
    public User getUserFromRequest(HttpServletRequest req) {
        UUID userId = getUserIdFromRequest(req);
        if (userId != null) {
            return userRepo.findById(userId).orElse(null);
        }
        return null;
    }

}
