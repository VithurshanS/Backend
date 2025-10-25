package com.tutoring.Tutorverse.Dto;

import com.tutoring.Tutorverse.Model.User;

public class UserGetDto {
    private String email;
    private String name;
    private String role;
    private boolean isEmailVerified;

    public UserGetDto(String email, String name, String role, boolean isEmailVerified) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.isEmailVerified = isEmailVerified;
    }

    public static UserGetDto sendUser(User user){
        return new UserGetDto(user.getEmail(), user.getName(), user.getRole().getName(), user.isEmailVerified());
    }

    // Getter methods for JSON serialization
    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }
}
