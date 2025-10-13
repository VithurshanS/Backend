package com.tutoring.Tutorverse.Admin.Dto;

public class TutorsSummaryDto {
    public long approved;
    public long pending;
    public long banned;

    public TutorsSummaryDto() {}

    public TutorsSummaryDto(long approved, long pending, long banned) {
        this.approved = approved;
        this.pending = pending;
        this.banned = banned;
    }
}
