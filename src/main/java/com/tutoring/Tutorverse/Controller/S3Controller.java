package com.tutoring.Tutorverse.Controller;


import com.tutoring.Tutorverse.Dto.MaterialDto;
import com.tutoring.Tutorverse.Services.EnrollmentService;
import com.tutoring.Tutorverse.Services.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.*;

import com.tutoring.Tutorverse.Services.S3Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tutoring.Tutorverse.Services.SendgridService;


@RestController
@RequestMapping("/api/materials")
public class S3Controller {

    private final S3Service s3Service;
    private final MaterialService materialService;
    private final SendgridService sendgridService;
    private final EnrollmentService enrollmentService;

    public S3Controller(
            S3Service s3Service,
            MaterialService materialService,
            SendgridService sendgridService,
            EnrollmentService enrollmentService
    ) {
        this.s3Service = s3Service;
        this.materialService = materialService;
        this.sendgridService = sendgridService;
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<MaterialDto> uploadFile(
            @RequestParam("module_id") UUID module_id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "link", required = false) String link) {

        try {

            String fileUrl = null;

            if ("Document".equalsIgnoreCase(type) ) {
                if (file == null || file.isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }

            // Save the file temporarily
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);
            // Upload and get the URL
            fileUrl = s3Service.uploadFile(tempFile.getAbsolutePath(), file.getOriginalFilename());
            // Save material to database
            }

            else if ("Link".equalsIgnoreCase(type)) {
                if (link == null || link.isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }

                fileUrl = link;
            }

            MaterialDto material = materialService.saveToDatabase(module_id, title, description, type, fileUrl);

            // Send email to students
            List<Object[]> enrolled_emails_and_names = enrollmentService.getStudentEmailsAndFirstNamesByModuleId(module_id);
            System.out.println("Enrolled students:");

            for (Object[] student : enrolled_emails_and_names) {
                String email = (String) student[0];
                String firstName = (String) student[1];
                System.out.println("Sending email to: " + email + " Name: " + firstName);

                sendgridService.sendContentUploadEmail(
                        email,
                        firstName,
                        material.getTitle(),
                        material.getUrl()
                );
            }


            return ResponseEntity.ok(material);

        }

        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
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

    @PostMapping("upload/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }

        // Save the file temporarily
        try{
            // Save the file temporarily
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);
            // Upload and get the URL
            String fileUrl = s3Service.uploadFile(tempFile.getAbsolutePath(), file.getOriginalFilename());
            return ResponseEntity.ok(fileUrl);
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error uploading image");
        }
    }

}



