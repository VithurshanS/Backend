package com.tutoring.Tutorverse.Controller;


import com.tutoring.Tutorverse.Dto.MaterialDto;
import com.tutoring.Tutorverse.Repository.MaterialRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;
import com.tutoring.Tutorverse.Services.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.*;

import com.tutoring.Tutorverse.Services.S3Service;


@RestController
@RequestMapping("/api/materials")
public class S3Controller {

    private final S3Service s3Service;

    @Autowired
    private MaterialService materialService;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<MaterialDto> uploadFile(@RequestParam("module_id") UUID module_id, @RequestParam("title") String title, @RequestParam("description") String description, @RequestParam("type") String type, @RequestParam("file") MultipartFile file) {
        try {
            // Save the file temporarily
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);
            // Upload and get the URL
            String fileUrl = s3Service.uploadFile(tempFile.getAbsolutePath(), file.getOriginalFilename());
            // Save material to database
            MaterialDto material = materialService.saveToDatabase(module_id, title, description, type, fileUrl);
            return ResponseEntity.ok(material);
        }

        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body((MaterialDto) List.of());
        }
    }

    @GetMapping("/fetchAll")
    public ResponseEntity<List<MaterialDto>> getAllMaterialsByModuleId(@RequestParam("module_id") UUID moduleId) {
        try {
            List<MaterialDto> materials = materialService.getAllMaterialsByModuleId(moduleId);
            return ResponseEntity.ok(materials);
        }

        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }


    }
}
