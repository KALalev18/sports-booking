package com.kalalev.sports.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private int userId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "FirstName", length = 100, nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "LastName", length = 100, nullable = false)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "Email", length = 100, nullable = false, unique = true)
    private String email;

    // BCrypt hash, never the plain-text password
    @Column(name = "Password", length = 100, nullable = false)
    private String password;

    @ManyToOne(optional = false)
    @JoinColumn(name = "RoleId", nullable = false)
    private UserRole role;

    public User() {}

    public User(String firstName, String lastName, String email, String password, UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean isAdmin() {
        return role != null && UserRole.ADMIN.equals(role.getRoleName());
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
