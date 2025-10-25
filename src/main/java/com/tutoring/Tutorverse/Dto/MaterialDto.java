package com.tutoring.Tutorverse.Dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialDto {
    private UUID material_id;
    private UUID moduleId;
    private String title;
    private String description;
    private String type;
    private String url;
}
