package com.tutoring.Tutorverse.Model;


import jakarta.persistence.*;

@Entity
@Table(name = "userss")
public class userDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private roleDto role;

    @Column(unique = true,name = "email")
    private String email;

    @Column(unique = true)
    private String providerid;

    @Column(name = "password",nullable = true)
    private String password;

    @Column(name = "name")
    private String name;

    public Long getId() {
        return id;
    }

    public roleDto getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getProviderid() {
        return providerid;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRole(roleDto role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProviderid(String providerid) {
        this.providerid = providerid;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }
}
