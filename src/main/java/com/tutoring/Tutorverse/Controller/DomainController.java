package com.tutoring.Tutorverse.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutoring.Tutorverse.Dto.DomainDto;
import com.tutoring.Tutorverse.Services.DomainService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domains")
public class DomainController {

    @Autowired
    private DomainService domainService;

    @GetMapping("/all")
    public ResponseEntity<List<DomainDto>> getAllDomains() {
        List<DomainDto> domains = domainService.getAllDomains();
        return ResponseEntity.ok(domains);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createDomain(@RequestBody DomainDto domainDto) {
        domainService.createDomain(domainDto);
        return ResponseEntity.ok("Domain created successfully");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteDomain(@PathVariable Integer id) {
        domainService.deleteDomain(id);
        return ResponseEntity.ok("Domain deleted successfully");
    }

}
