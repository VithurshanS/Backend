package com.tutoring.Tutorverse.Services;
import com.tutoring.Tutorverse.Dto.UserCreateDto; // Fixed package name casing (Dto vs DTO)
import com.tutoring.Tutorverse.Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;


@Component
public class Oauth2SuccessHandlerServices implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(Oauth2SuccessHandlerServices.class);

    @Autowired
    private JwtServices jwtServices;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Autowired
    private UserService userService;


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
            logger.info("=== OAuth2 User Creation Debug ===");
            logger.info("Email: {}", email);
            logger.info("Provider ID: {}", providerdId);
            logger.info("Name: {}", name);
            logger.info("Role from session: {}", role);
            logger.info("Creating UserCreateDto.googleUser...");

            Optional<User> newUser = userService.addUser(UserCreateDto.googleUser(email,role,providerdId,name));

            logger.info("UserService.addUser result: {}", newUser.isPresent() ? "SUCCESS" : "FAILED");
            if (newUser.isPresent()) {
                logger.info("Created/Retrieved User ID: {}", newUser.get().getId());
                logger.info("User Email: {}", newUser.get().getEmail());
                logger.info("User Role: {}", newUser.get().getRole().getName());
            }
            logger.info("================================");

            // Ensure we have a valid user
            if (newUser.isEmpty()) {
                throw new RuntimeException("Failed to create or retrieve user");
            }

            // Ensure we have the saved user with a valid ID


            // Generate JWT token
            String jwttoken = jwtServices.generateJwtToken(newUser.get());

            // Store JWT in cookie for session management (environment-aware configuration)
            setJwtCookie(response, jwttoken);

            // Check for custom redirect URI in session
            String customRedirectUri = null;
            if(session != null){
                customRedirectUri = (String) session.getAttribute("custom_redirect_uri");
                // Clean up session attributes
                session.removeAttribute("signup_role");
                session.removeAttribute("custom_redirect_uri");
            }

            // Use custom redirect URI if provided, otherwise use default frontend URL
            String redirUrl;
            if (customRedirectUri != null && !customRedirectUri.isEmpty()) {
                redirUrl = customRedirectUri;
                logger.info("Using custom redirect URI: {}", redirUrl);
            } else {
                redirUrl = frontendUrl;
                logger.info("Using default frontend URL: {}", redirUrl);
            }

            response.sendRedirect(redirUrl);
        } catch (Exception e) {
            // Enhanced error logging to identify the exact issue
            logger.error("=== OAuth2 Success Handler Error ===");
            logger.error("Error Type: {}", e.getClass().getSimpleName());
            logger.error("Error Message: {}", e.getMessage());
            logger.error("Full Stack Trace:", e);
            logger.error("===============================");
            response.sendRedirect("/error?message=oauth2_callback_failed&details=" + e.getClass().getSimpleName());
        }
    }

    /**
     * Helper method to set JWT cookie with environment-aware configuration
     */
    private void setJwtCookie(HttpServletResponse response, String jwtToken) {
        boolean isProduction = "prod".equals(activeProfile);

        if (isProduction) {
            // Production: Cross-domain HTTPS configuration
            response.setHeader("Set-Cookie", String.format(
                "jwt_token=%s; Path=/; Max-Age=86400; HttpOnly=false; Secure=true; SameSite=None; Domain=.%s",
                jwtToken,baseUrl
            ));
            logger.info("--------------------------------------"+response.getHeader("Set-Cookie"));
        } else {
            // Development: Local HTTP configuration
            Cookie jwtCookie = new Cookie("jwt_token", jwtToken);
            jwtCookie.setHttpOnly(false); // Frontend needs to read this
            jwtCookie.setSecure(false); // HTTP in development
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400); // 1 day
            response.addCookie(jwtCookie);
        }
    }
}
