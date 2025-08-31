package com.tutoring.Tutorverse.Controller;


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
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
public class AuthController {

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



    @GetMapping("/home/callback")
    public String handleOAuth2Callback(@RequestParam("token") String token) {
        // This endpoint handles the OAuth2 callback with the JWT token
        return String.format("""
            <html>
            <head><title>Login Successful</title></head>
            <body>
                <h2>✅ Login Successful!</h2>
                <p>Your JWT token has been stored in both localStorage and HTTP-only cookie for session management.</p>
                <div style="margin: 20px 0; padding: 15px; background: #f8f9fa; border-left: 4px solid #007bff;">
                    <strong>Token:</strong> <code style="word-break: break-all;">%s</code>
                </div>
                
                <h3>Session Management:</h3>
                <ul>
                    <li>✅ JWT stored in HTTP-only cookie (secure)</li>
                    <li>✅ JWT stored in localStorage (for API calls)</li>
                    <li>✅ Session will persist across page refreshes</li>
                    <li>✅ Automatic authentication for protected pages</li>
                </ul>
                
                <div style="margin-top: 20px;">
                    <a href="/api/user/profile" style="margin-right: 10px; padding: 10px 15px; background: #007bff; color: white; text-decoration: none; border-radius: 5px;">Test Protected Endpoint</a>
                    <a href="/home" style="margin-right: 10px; padding: 10px 15px; background: #28a745; color: white; text-decoration: none; border-radius: 5px;">Go to Home</a>
                    <a href="/logout" style="padding: 10px 15px; background: #dc3545; color: white; text-decoration: none; border-radius: 5px;">Logout</a>
                </div>
                
                <script>
                    // Store token in localStorage for frontend API calls
                    localStorage.setItem('jwt_token', '%s');
                    console.log('JWT token stored in both cookie and localStorage');
                    
                    // Display cookie status
                    const cookieExists = document.cookie.split(';').some((item) => item.trim().startsWith('jwt_token='));
                    if (cookieExists) {
                        console.log('✅ JWT cookie successfully set');
                    } else {
                        console.log('⚠️ JWT cookie not detected');
                    }
                </script>
            </body>
            </html>
            """, token, token);
    }

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body,HttpServletResponse response) {
        String email = body.get("email");
        String password = body.get("password");
        String role = body.get("role");
        String name = body.get("name");

        Optional<User> addedUser = userService.addUser(UserCreateDto.emailUser(email,name,password,role));


        if (addedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        String token = jwtServices.generateJwtToken(addedUser.get());

        // Store JWT in HTTP-only cookie for session management (same as OAuth2)
        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to true in production with HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(86400); // 1 day
        response.addCookie(jwtCookie);
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

        // Store JWT in HTTP-only cookie for session management (same as OAuth2)
        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to true in production with HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(86400); // 1 day
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(Map.of(
            "token", token,
            "message", "Login successful",
            "user", Map.of(
                "email", user.getEmail(),
                "name", user.getName() != null ? user.getName() : "",
                "role", user.getRole().getName()
            )
        ));
    }
    @GetMapping("/")
    public String home() {
        return """
            <h2>OAuth2 JWT Demo</h2>
            <p>Login with different roles:</p>
            <ul>
                <li><a href='/oauth2/authorization/google?role=TUTOR'>Login as USER</a></li>
                <li><a href='/oauth2/authorization/google?role=ADMIN'>Login as ADMIN</a></li>
                <li><a href='/oauth2/authorization/google?role=STUDENT'>Login as MODERATOR</a></li>
            </ul>
            <p><strong>Note:</strong> Role only applies to new user registration. Existing users keep their current roles.</p>
            """;
    }
    @GetMapping("/oauth2/login/{role}")
    public void oauthLogin(@PathVariable String role, HttpServletResponse response) throws IOException {
        // Validate role parameter
        String upperRole = role.toUpperCase();
        if (!upperRole.equals("TUTOR") && !upperRole.equals("ADMIN") && !upperRole.equals("STUDENT")) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid role parameter");
            return;
        }

        response.sendRedirect("/oauth2/authorization/google?role=" + upperRole);
    }

    @GetMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt_token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

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
}