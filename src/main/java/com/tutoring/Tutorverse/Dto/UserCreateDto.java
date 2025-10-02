package com.tutoring.Tutorverse.Dto;

public class UserCreateDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
    private String providerId;
    private boolean isEmailVerified;


    public UserCreateDto() {}

    public UserCreateDto(String email, String password, String firstName, String lastName, String role, String providerId, boolean isEmailVerified) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.providerId = providerId;
        this.isEmailVerified = isEmailVerified;
    }

    public static UserCreateDto emailUser(String email, String firstName, String lastName, String password, String role){
        return new UserCreateDto(email, password, firstName, lastName, role, null, false);
    }

    public static UserCreateDto googleUser(String email, String role, String providerId, String firstName, String lastName){
        return new UserCreateDto(email, null, firstName, lastName, role, providerId, true);
    }



    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.isEmailVerified = emailVerified;
    }
}
