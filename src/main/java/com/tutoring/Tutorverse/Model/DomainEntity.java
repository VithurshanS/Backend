package com.tutoring.Tutorverse.Model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "domain")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "domain_id", nullable = false, updatable = false)
    private Integer domainId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

}
