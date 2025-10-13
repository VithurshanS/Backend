package com.tutoring.Tutorverse.Controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tutoring.Tutorverse.Dto.ModuelDescriptionDto;
import com.tutoring.Tutorverse.Services.ModuelDescriptionService;
import com.tutoring.Tutorverse.Services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/module-descriptions")
public class ModuelDescriptionController {

    @Autowired
    private ModuelDescriptionService service;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ModuelDescriptionDto> create(@RequestBody ModuelDescriptionDto dto, HttpServletRequest req) {
        UUID userId = userService.getUserIdFromRequest(req);
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        ModuelDescriptionDto created = service.create(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/exits")
    public ResponseEntity<Boolean> exists(@RequestParam UUID moduleId) {
        boolean exists = service.exists(moduleId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping
    public ResponseEntity<ModuelDescriptionDto> getByModuleId(@RequestParam UUID moduleId) {
        return ResponseEntity.ok(service.getByModuleId(moduleId));
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<ModuelDescriptionDto> update(@PathVariable UUID moduleId, @RequestBody ModuelDescriptionDto dto, HttpServletRequest req) {
        UUID userId = userService.getUserIdFromRequest(req);
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        ModuelDescriptionDto updated = service.update(userId, moduleId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{moduleId}")
    public ResponseEntity<Void> delete(@PathVariable UUID moduleId, HttpServletRequest req) {
        UUID userId = userService.getUserIdFromRequest(req);
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        service.delete(userId, moduleId);
        return ResponseEntity.noContent().build();
    }
}
