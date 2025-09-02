package com.tutoring.Tutorverse.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;





@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "material")
public class MaterialEntity {

    @Id
    @GeneratedValue
    @Column(name = "material_id", nullable = false, updatable = false)
    private UUID material_id;

    @ManyToOne
    @JoinColumn(name = "module_id", referencedColumnName = "module_id")
    @JsonIgnore
    private ModuelsEntity module;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "url", nullable = false)
    private String url;
}
