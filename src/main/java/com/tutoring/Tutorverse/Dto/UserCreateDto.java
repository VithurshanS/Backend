package com.tutoring.Tutorverse.Dto;

public class UserCreateDto {
    private String email;
    private String password;
    private String name;
    private String role;
    private String providerId;
    private boolean isEmailVerified;


    public UserCreateDto() {}

    public UserCreateDto(String email, String password, String name, String role, String providerId, boolean isEmailVerified) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.providerId = providerId;
        this.isEmailVerified = isEmailVerified;
    }

    public static UserCreateDto emailUser(String email,String name,String password,String role){
        return new UserCreateDto(email,password,name,role,null,false);
    }

    public static UserCreateDto googleUser(String email,String role,String providerId,String name){
        return new UserCreateDto(email,null,name,role,providerId,true);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
