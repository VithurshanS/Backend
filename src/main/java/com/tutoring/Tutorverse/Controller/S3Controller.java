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
            List<String> enrolled_emails = enrollmentService.getStudentEmailsByModuleId(module_id);
            System.out.println("email list" + enrolled_emails);
            for (String email : enrolled_emails) {
                sendgridService.sendContentUploadEmail(
                                email,
                                "New Material Has Been Uploaded: " + material.getTitle(),
                                "Dear Student, \n A new course material han been uploaded\n\n" +
                                        ".\n\n" +
                                        "Title: " + material.getTitle() + "\n" +
                                        "🔗 Access it here: " + material.getUrl() + "\n\n" +
                                        "We encourage you to review this material at your earliest convenience to stay updated with your course progress.\n\n" +
                                        "Best regards,\n" +
                                        "Tutorverse Team"
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
}
