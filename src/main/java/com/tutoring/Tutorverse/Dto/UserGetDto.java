package com.tutoring.Tutorverse.Dto;

import com.tutoring.Tutorverse.Model.User;

public class UserGetDto {
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean isEmailVerified;

    public UserGetDto(String email, String firstName, String lastName, String role, boolean isEmailVerified) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.isEmailVerified = isEmailVerified;
    }

    public static UserGetDto sendUser(User user){
        return new UserGetDto(user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole().getName(), user.isEmailVerified());
    }

    // Getter methods for JSON serialization
    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRole() {
        return role;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }
}
