package com.tutoring.Tutorverse.Model;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import jakarta.persistence.*;
@Entity
@Table(name = "report")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

@Id
@GeneratedValue(strategy = GenerationType.AUTO)
@Column(name = "report_id", nullable = false, updatable = false)
private UUID reportId;

@Column(name = "module_id", nullable = false)
private UUID moduleId;

@Column(name = "reason", nullable = false, columnDefinition = "TEXT")
private String reason;

@Column(name = "reported_by", nullable = false)
private UUID reportedBy;

@Column(name = "report_date", nullable = false)
private LocalDateTime reportDate;

@Column(name = "status", nullable = false)
@Enumerated(EnumType.STRING)
private ReportStatus status = ReportStatus.PENDING;

public enum ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED
}
}
