package com.tutoring.Tutorverse.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tutoring.Tutorverse.Dto.UserCreateDto;
import com.tutoring.Tutorverse.Dto.UserGetDto;
import com.tutoring.Tutorverse.Model.Role;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.Services.JwtServices;
import com.tutoring.Tutorverse.Services.RoleService;
import com.tutoring.Tutorverse.Services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Autowired
    private userRepository userRepo;
    @Autowired
    private UserService userService;

    @Autowired
    private JwtServices jwtServices;

    @Autowired
    private RoleService roleService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/home")
    public String home(Authentication authentication) {
        return "home";
    }





    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body,HttpServletResponse response) {
        String email = body.get("email");
        String password = body.get("password");
        String role = body.get("role");
        String firstName = body.get("first_name");
        String lastName = body.get("last_name");

        Optional<User> addedUser = userService.addUser(UserCreateDto.emailUser(email,firstName,lastName,password,role));


        if (addedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        String token = jwtServices.generateJwtToken(addedUser.get());

        // Store JWT in cookie using helper method (automatically handles dev/prod)
        setJwtCookie(response, token);

        return ResponseEntity.ok("User registered");
    }

    @GetMapping("/api/getuser")
    public ResponseEntity<?> getuser(HttpServletRequest req){
        try{
            String token = null;
            User user = null;

            if(req.getCookies() != null){
                for(Cookie cookie : req.getCookies()){
                    if("jwt_token".equals(cookie.getName())){
                        token = cookie.getValue();
                        String email = jwtServices.getEmailFromJwtToken(token);

                        Optional<User> userOpt = userRepo.findByEmail(email);

                        if(userOpt.isPresent()){
                            user = userOpt.get();
                            break;
                        }
                    }
                }
            }

            if(user == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or invalid token");
            }


            return ResponseEntity.ok(Map.of("user", UserGetDto.sendUser(user)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or authentication failed");
        }
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String email = body.get("email");
        String password = body.get("password");

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty() || userOpt.get().getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtServices.generateJwtToken(user);

        // Store JWT in cookie using helper method (automatically handles dev/prod)
        setJwtCookie(response, token);

        return ResponseEntity.ok(Map.of(
            "token", token,
            "message", "Login successful",
            "user", Map.of(
                "email", user.getEmail(),
                "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                "lastName", user.getLastName() != null ? user.getLastName() : "",
                "role", user.getRole().getName()
            )
        ));
    }
    // @GetMapping("/")
    // public String home() {
    //     return """
    //         <h2>OAuth2 JWT Demo</h2>
    //         <p>Login with different roles:</p>
    //         <ul>
    //             <li><a href='/oauth2/authorization/google?role=TUTOR'>Login as USER</a></li>
    //             <li><a href='/oauth2/authorization/google?role=ADMIN'>Login as ADMIN</a></li>
    //             <li><a href='/oauth2/authorization/google?role=STUDENT'>Login as MODERATOR</a></li>
    //         </ul>
    //         <p><strong>Note:</strong> Role only applies to new user registration. Existing users keep their current roles.</p>
    //         """;
    // }
    @GetMapping("/oauth2/login/{role}")
    public void oauthLogin(@PathVariable String role, 
                          @RequestParam(value = "redirect_uri", required = false) String redirectUri,
                          HttpServletResponse response, 
                          HttpServletRequest request) throws IOException {
        // Validate role parameter
        String upperRole = role.toUpperCase();
        if (!upperRole.equals("TUTOR") && !upperRole.equals("ADMIN") && !upperRole.equals("STUDENT")) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid role parameter");
            return;
        }

        // Store role and custom redirect URI in session for the success handler
        HttpSession session = request.getSession(true);
        session.setAttribute("signup_role", upperRole);
        
        // Store custom redirect URI if provided
        if (redirectUri != null && !redirectUri.isEmpty()) {
            session.setAttribute("custom_redirect_uri", redirectUri);
            logger.info("Storing custom redirect URI in session: {}", redirectUri);
        }
        
        logger.info("Starting OAuth2 login for role: {}", upperRole);
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        // Log current user for debugging
        UUID currentUserId = userService.getUserIdFromRequest(request);


        logger.info("=== LOGOUT REQUEST ===");
        logger.info("Current User ID: {}", currentUserId);
        

        // Clear JWT cookie using helper method (automatically handles dev/prod)
        clearJwtCookie(response);

        // Clear Spring Security context
        SecurityContextHolder.clearContext();

        
        logger.info("Logout completed - cookie cleared and security context cleared");

        return ResponseEntity.ok().body("{\"message\": \"Logged out successfully\"}");
    }
    @PostMapping("/sum/post")
    public String pp(@RequestBody Map<String,Object> ss){
        return (String) ss.get("sum");

    }
    @GetMapping("/api/user/profile")
    public Map<String, Object> getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Map.of(
                "message", "Protected resource accessed successfully!",
                "user", auth.getName(),
                "authorities", auth.getAuthorities()
        );
    }

    @GetMapping("/api/public/test")
    public Map<String, String> publicEndpoint() {
        return Map.of("message", "This is a public endpoint");
    }

    @GetMapping("/debug/oauth2")
    public Map<String, Object> debugOAuth2() {
        return Map.of(
            "message", "OAuth2 Debug Information",
            "expected_redirect_uri", "http://localhost:8080/login/oauth2/code/google",
            "frontend_url", frontendUrl,
            "ssl_enabled", sslEnabled,
            "note", "Make sure this exact redirect_uri is configured in Google Cloud Console"
        );
    }


//    public String logout(HttpServletResponse response) {
//        // Clear the JWT cookie
//        Cookie jwtCookie = new Cookie("jwt_token", null);
//        jwtCookie.setHttpOnly(true);
//        jwtCookie.setPath("/");
//        jwtCookie.setMaxAge(0); // Delete the cookie
//        response.addCookie(jwtCookie);
//
//        return """
//            <html>
//            <head><title>Logged Out</title></head>
//            <body>
//                <h2>✅ Successfully Logged Out</h2>
//                <p>Your session has been cleared.</p>
//                <script>
//                    // Clear localStorage as well
//                    localStorage.removeItem('jwt_token');
//                    setTimeout(() => {
//                        window.location.href = '/';
//                    }, 2000);
//                </script>
//            </body>
//            </html>
//            """;
//    }

    /**
     * Helper method to set JWT cookie with production-ready configuration
     * Automatically handles development vs production settings
     */
    private void setJwtCookie(HttpServletResponse response, String token) {
        // Determine if we're in production (HTTPS) or development
        boolean isProduction = sslEnabled || frontendUrl.startsWith("https://");

        if (isProduction) {
            // Production configuration: Secure, SameSite=None, cross-domain
            response.setHeader("Set-Cookie", String.format(
                "jwt_token=%s; Path=/; Max-Age=86400; Secure; SameSite=None; Domain=.shancloudservice.com",
                token
            ));
        } else {
            // Development configuration: Non-secure, SameSite=Lax, localhost
            Cookie jwtCookie = new Cookie("jwt_token", token);
            jwtCookie.setHttpOnly(false);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400); // 1 day
            response.addCookie(jwtCookie);
        }
    }

    /**
     * Helper method to clear JWT cookie
     */
    private void clearJwtCookie(HttpServletResponse response) {
        boolean isProduction = sslEnabled || frontendUrl.startsWith("https://");

        if (isProduction) {
            // Production: Clear secure cookie with multiple variations to ensure cleanup
            response.addHeader("Set-Cookie",
                "jwt_token=; Path=/; Max-Age=0; Secure; SameSite=None; Domain=.shancloudservice.com"
            );
            // Also clear without domain to catch any cookies set without domain
            response.addHeader("Set-Cookie",
                "jwt_token=; Path=/; Max-Age=0; Secure; SameSite=None"
            );
        } else {
            // Development: Clear non-secure cookie with multiple variations
            Cookie jwtCookie = new Cookie("jwt_token", "");
            jwtCookie.setHttpOnly(false);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(0);
            response.addCookie(jwtCookie);

            // Also add Set-Cookie header as backup
            response.addHeader("Set-Cookie", "jwt_token=; Path=/; Max-Age=0");
        }
    }
}
