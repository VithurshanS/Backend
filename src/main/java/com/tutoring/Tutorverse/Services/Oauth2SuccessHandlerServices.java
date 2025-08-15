package com.tutoring.Tutorverse.Services;


import com.tutoring.Tutorverse.DTO.UserCreateDto;
import com.tutoring.Tutorverse.Model.Role;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.userRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class Oauth2SuccessHandlerServices implements AuthenticationSuccessHandler {
//    private final userRepository userRepository;

    @Autowired
    private JwtServices jwtServices;

//    @Autowired
//    private RoleService roleService;

    @Autowired
    private UserService userService;

//    public Oauth2SuccessHandlerServices(userRepository userRepository) {
//
//    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User othUser = (OAuth2User) authentication.getPrincipal();
            String email = othUser.getAttribute("email");
            String providerdId = othUser.getAttribute("sub");
            String name = othUser.getAttribute("name");

            HttpSession session  = request.getSession(false);
            String role;
            if(session != null){
                role = (String) session.getAttribute("signup_role");
            } else {
                role = null;
            }

            // Debug logging for OAuth2 user creation
            System.out.println("=== OAuth2 User Creation Debug ===");
            System.out.println("Email: " + email);
            System.out.println("Provider ID: " + providerdId);
            System.out.println("Name: " + name);
            System.out.println("Role from session: " + role);
            System.out.println("Creating UserCreateDto.googleUser...");

            Optional<User> newUser = userService.addUser(UserCreateDto.googleUser(email,role,providerdId,name));
            
            System.out.println("UserService.addUser result: " + (newUser.isPresent() ? "SUCCESS" : "FAILED"));
            if (newUser.isPresent()) {
                System.out.println("Created/Retrieved User ID: " + newUser.get().getId());
                System.out.println("User Email: " + newUser.get().getEmail());
                System.out.println("User Role: " + newUser.get().getRole().getName());
            }
            System.out.println("================================");
            
            // Ensure we have a valid user
            if (newUser.isEmpty()) {
                throw new RuntimeException("Failed to create or retrieve user");
            }

            // Ensure we have the saved user with a valid ID


            if(session != null){
                session.removeAttribute("signup_role");
            }

            String jwttoken = jwtServices.generateJwtToken(newUser.get());

            // Store JWT in HTTP-only cookie for session management
            Cookie jwtCookie = new Cookie("jwt_token", jwttoken);
            jwtCookie.setHttpOnly(true); // Prevents JavaScript access (XSS protection)
            jwtCookie.setSecure(false); // Set to true in production with HTTPS
            jwtCookie.setPath("/"); // Available for entire application
            jwtCookie.setMaxAge(86400); // 1 day (same as JWT expiration)
            response.addCookie(jwtCookie);

            String redirUrl = String.format("http://localhost:3000");
            response.sendRedirect(redirUrl);
        } catch (Exception e) {
            // Enhanced error logging to identify the exact issue
            System.err.println("=== OAuth2 Success Handler Error ===");
            System.err.println("Error Type: " + e.getClass().getSimpleName());
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("Full Stack Trace:");
            e.printStackTrace();
            System.err.println("===============================");
            response.sendRedirect("/error?message=oauth2_callback_failed&details=" + e.getClass().getSimpleName());
        }
    }
}
