package com.tutoring.Tutorverse.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tutoring.Tutorverse.Dto.ModuelsDto;
import com.tutoring.Tutorverse.Services.ModulesService;
import com.tutoring.Tutorverse.Services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;



@RestController
@RequestMapping("/api/modules")
public class ModulesController {
    
    @Autowired
    private ModulesService modulesService;

    @Autowired
    private UserService userService;

    @Autowired
    private userRepository userRepo;

    @Autowired
    private ModulesRepository modulesRepository;


    @GetMapping
    public ResponseEntity<List<ModuelsDto>> getAllModules() {
        List<ModuelsDto> modules = modulesService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/get-modulesfortutor")
    public ResponseEntity<List<ModuelsDto>> getTutorModules(HttpServletRequest req){
        UUID userId = userService.getUserIdFromRequest(req);
        if(userId == null || !userService.isTutor(userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requires TUTOR role");
        }
        List<ModuelsDto> modules = modulesService.getModulesByTutorId(userId);
        return ResponseEntity.ok(modules);

    }


    @GetMapping("/domain/{id}")
    public ResponseEntity<List<ModuelsDto>> getModulesByDomainId(@PathVariable Integer id) {
        List<ModuelsDto> modules = modulesService.getModulesByDomainId(id);
        return ResponseEntity.ok(modules);
    }
    


    @PostMapping("/create")
    public ResponseEntity<?> createModule(@RequestBody ModuelsDto moduelsDto, HttpServletRequest req) {
        User tutor = requireTutor(req);
        // Force tutorId from token, ignore any client-sent tutorId
        moduelsDto.setTutorId(tutor.getId());
        modulesService.createModule(moduelsDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Module created successfully");
    }


    @DeleteMapping("/delete/{moduleId}")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID moduleId, HttpServletRequest req) {
        User tutor = requireTutor(req);
        Optional<ModuelsEntity> moduleOpt = modulesRepository.findById(moduleId);
        if (moduleOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }
        ModuelsEntity module = moduleOpt.get();
        if (!module.getTutorId().equals(tutor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of module");
        }
        modulesService.deleteModule(moduleId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/search")
    public ResponseEntity<List<ModuelsDto>> searchModules(@RequestParam String query) {
        List<ModuelsDto> results = modulesService.searchModules(query);
        return ResponseEntity.ok(results);
    }

    private User requireTutor(HttpServletRequest req) {
        UUID userId = userService.getUserIdFromRequest(req);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getRole() == null || user.getRole().getName() == null || !user.getRole().getName().equalsIgnoreCase("TUTOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requires TUTOR role");
        }
        return user;
    }

    @GetMapping("/tutor")
    public ResponseEntity<List<ModuelsDto>> getModulesByTutorId(HttpServletRequest req) {
        User tutor = requireTutor(req);
        List<ModuelsDto> modules = modulesService.getModulesByTutorId(tutor.getId());
        System.out.println(modules);
        return ResponseEntity.ok(modules);
    }
}
