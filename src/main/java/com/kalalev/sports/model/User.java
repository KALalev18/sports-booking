package com.kalalev.sports.model;

import jakarta.persistence.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId", columnDefinition = "int", nullable = false)
    private int userId;
    @Column(name = "FirstName", columnDefinition = "nvarchar(100)", nullable = false)
    private String firstName;
    @Column(name = "LastName", columnDefinition = "nvarchar(100)", nullable = false)
    private String lastName;
    @Column(name = "Email", columnDefinition = "nvarchar(100)", nullable = false)
    private String email;
    @Column(name = "Password", columnDefinition = "nvarchar(100)", nullable = false)
    private String password;
    @ManyToOne
    @JoinColumn(name = "RoleId")
    @Column(name = "RoleId", columnDefinition = "int", nullable = false)
    private UserRole roleId;

    public User(int userId, String firstName, String lastName, String email, String password, UserRole roleId) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
    }
    public User(){}

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

    public UserRole getRoleId() {
        return roleId;
    }

    public void setRoleId(UserRole roleId) {
        this.roleId = roleId;
    }

}
