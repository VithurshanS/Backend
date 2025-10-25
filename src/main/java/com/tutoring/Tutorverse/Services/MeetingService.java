package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Dto.MeetingRequestDto;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.EnrollRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;
import com.tutoring.Tutorverse.Repository.ScheduleRepository;
import com.tutoring.Tutorverse.Repository.userRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MeetingService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private userRepository userRepo;

    @Autowired
    private JwtServices jwtServices;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private ModulesRepository modulesRepository;
    @org.springframework.beans.factory.annotation.Value("${app.jitsi.appid}")
    private String JITSI_APP_ID;

    @org.springframework.beans.factory.annotation.Value("${app.jitsi.secret}")
    private String JITSI_APP_SECRET;

    @org.springframework.beans.factory.annotation.Value("${app.jitsi.domain}")
    private String JITSI_DOMAIN;

    public Map<String, Object> createMeeting(MeetingRequestDto details, String authToken) {
        try {
            // Step 1: Get user ID and validate token
            if (!jwtServices.validateJwtToken(authToken)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            UUID userId = jwtServices.getUserIdFromJwtToken(authToken);
            Long roleId = jwtServices.getRoleIdFromJwtToken(authToken);
            String userEmail = jwtServices.getEmailFromJwtToken(authToken);

            // Step 2: Obtain user role and details
            Optional<User> userOpt = userRepo.findById(userId);
            if (userOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            User user = userOpt.get();
            String roleName = user.getRole().getName();
            if(isStudent(userId)){
                if(!enrollRepository.existsByStudentStudentIdAndModuleModuleId(userId, details.getModuleId())){
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student is not enrolled in the module");
                }
            }
            if(isTutor(userId)){
                if(!modulesRepository.existsByTutorIdAndModuleId(userId, details.getModuleId())){
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tutor does not own the module");
                }
            }




            UUID scheduleId = scheduleRepository.findMatchingSchedule(
                details.getRequestedDate(),
                details.getRequestedTime(),
                details.getModuleId()
            );

            if (scheduleId == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No matching schedule found for the requested date, time and module");
            }

            // Step 4: Generate token using function (tutor-moderator, student-guest)
            boolean isModerator = "TUTOR".equals(roleName);
            String cn = modulesRepository.findByModuleId(details.getModuleId())
                .map(m -> m.getName())
                .orElse("Unknown Module");
            String jitsiToken = generateJitsiToken(user, scheduleId.toString(), isTutor(userId));

            // Step 5: Generate meeting link using token and schedule ID as room ID

            String meetingLink = generateMeetingLink(jitsiToken, cn);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("meetingLink", meetingLink);
            response.put("scheduleId", scheduleId);
            response.put("roomId", scheduleId.toString());
            response.put("userRole", roleName);
            response.put("token", jitsiToken);
            response.put("userDetails", Map.of(
                "userId", userId,
                "email", userEmail,
                "name", user.getName() != null ? user.getName() : userEmail
            ));
            response.put("meetingDetails", Map.of(
                "moduleId", details.getModuleId(),
                "requestedDate", details.getRequestedDate(),
                "requestedTime", details.getRequestedTime()
            ));
            response.put("courseName", modulesRepository.findByModuleId(details.getModuleId())
                .map(m -> m.getName())
                .orElse("Unknown Module"));

            return response;

        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to create meeting: " + e.getMessage());
        }
    }

    private String generateJitsiToken(User user, String roomId, boolean isModerator) {
        try {
            long nowMillis = System.currentTimeMillis();
            long expMillis = nowMillis + 3600_000; // 1 hour expiration

            // Build context.user for Jitsi
            Map<String, Object> userContext = new HashMap<>();
            userContext.put("name", user.getName() != null ? user.getName() : user.getEmail());
            userContext.put("email", user.getEmail());
            userContext.put("id", user.getId().toString());
            userContext.put("affiliation", isModerator ? "owner" : "member");
//            userContext.put("moderator", isModerator); // true for TUTOR, false for

            Map<String, Object> context = new HashMap<>();
            context.put("user", userContext);

            // Convert app secret to bytes
            byte[] keyBytes = JITSI_APP_SECRET.getBytes(StandardCharsets.UTF_8);

            // Build JWT token for Jitsi
            String token = Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setAudience("jitsi")
                    .setIssuer(JITSI_APP_ID)
                    .setSubject(JITSI_DOMAIN)
                    .claim("room", roomId)
                    .setExpiration(new Date(expMillis))
                    .claim("context", context)
                    .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
                    .compact();

            return token;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to generate Jitsi token: " + e.getMessage());
        }
    }

    private String generateMeetingLink(String token, String roomId) {
        // Generate the complete Jitsi meeting URL with token
        return String.format("https://%s/%s?jwt=%s", JITSI_DOMAIN, roomId, token);
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
