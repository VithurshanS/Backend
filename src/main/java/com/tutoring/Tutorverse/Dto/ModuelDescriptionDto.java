package com.tutoring.Tutorverse.Dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuelDescriptionDto {

    private Long id;
    private UUID moduleId;

    private String name;
    private String domain;
    private Integer price;
    private String tutorName;

    // Point-wise description for the frontend
    @Builder.Default
    private List<String> descriptionPoints = new ArrayList<>();
}
