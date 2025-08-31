package com.tutoring.Tutorverse.Model;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
@Table(name = "usersss")
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonManagedReference
    private Role role;

    @Column(unique = true,name = "email")
    private String email;

    @Column(unique = true)
    private String providerid;

    @Column(name = "password",nullable = true)
    private String password;

    @Column(name = "name")
    private String name;
    
    @Column(name = "is_email_verified", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean is_email_verified = false; // Java field default

    public UUID getId() {
        return id;
    }

    public Role getRole() {
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

    public boolean isEmailVerified() {
        return is_email_verified;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setRole(Role role) {
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

    public void setEmailVerified(boolean emailVerified) {
        this.is_email_verified = emailVerified;
    }
}