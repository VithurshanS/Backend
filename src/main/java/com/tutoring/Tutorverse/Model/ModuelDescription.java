package com.tutoring.Tutorverse.Model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "module_description")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ModuelDescription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "module_id", nullable = true, updatable = false)
    private UUID moduleId;

    @Column(name = "name", nullable = false)
    private String name = "Sample Module";

    @Column(name = "domain", nullable = false)
    private String domain = "Sample Domain";
    
    @Column(name = "price", nullable = false)
    private Integer price = 0;

    @Column(name = "tutor_name", nullable = false)
    private String tutorName = "MR.X";

    // Use TEXT to support long point-wise descriptions
    @jakarta.persistence.Lob
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description = "No description available";


}
