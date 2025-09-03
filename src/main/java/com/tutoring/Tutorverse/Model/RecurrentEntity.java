package com.tutoring.Tutorverse.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recurrent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurrentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recurrent_id", nullable = false, updatable = false)
    private Integer recurrentId;

    @Column(name = "recurrent_type", nullable = false, length = 50)
    private String recurrentType;
}
