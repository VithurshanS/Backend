package com.tutoring.Tutorverse.Controller;


import com.tutoring.Tutorverse.Services.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.tutoring.Tutorverse.Services.SendgridService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/emails")


public class EmailController {

    @Autowired
    private EnrollmentService enrollmentService ;


    @GetMapping("/enrolled")
    public List<String> getStudentEmails(@RequestParam("module_id") UUID moduleId) {
        return enrollmentService.getStudentEmailsByModuleId(moduleId);
    }

}
