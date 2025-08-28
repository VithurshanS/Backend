package com.tutoring.Tutorverse.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tutoring.Tutorverse.Dto.ModuelsDto;
import com.tutoring.Tutorverse.Services.ModulesService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/modules")
public class ModulesController {
    
    @Autowired
    private ModulesService modulesService;


    @GetMapping
    public ResponseEntity<List<ModuelsDto>> getAllModules() {
        List<ModuelsDto> modules = modulesService.getAllModules();
        return ResponseEntity.ok(modules);
    }


    @PostMapping("/create")
    public ResponseEntity<String> createModule(@RequestBody ModuelsDto moduelsDto) {
        modulesService.createModule(moduelsDto);
        return ResponseEntity.status(201).body("Module created successfully");
    }


    @DeleteMapping("/delete/{moduleId}")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID moduleId) {
        modulesService.deleteModule(moduleId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/search")
    public ResponseEntity<List<ModuelsDto>> searchModules(@RequestParam String query) {
        List<ModuelsDto> results = modulesService.searchModules(query);
        return ResponseEntity.ok(results);
    }


}
