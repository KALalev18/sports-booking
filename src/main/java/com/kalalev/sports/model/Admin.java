package com.kalalev.sports.model;

import jakarta.persistence.*;

@Entity
@Table(name="Admins")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AdminId", columnDefinition = "int", nullable = false)
    private int AdminId;
    @Column(name = "Email", columnDefinition = "nvarchar(50)", nullable = false)
    private String email;
    @Column(name = "Password", columnDefinition = "nvarchar(50)", nullable = false)
    private String password;
    @OneToOne
    @JoinColumn(name = "RoleId")
    @Column(name = "RoleId", columnDefinition = "int", nullable = false)
    private UserRole roleId;

    public Admin(int adminId, String email, String password, UserRole roleId) {
        this.AdminId = adminId;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
    }
    public Admin(){}

    public int getAdminId() {
        return AdminId;
    }

    public void setAdminId(int adminId) {
        AdminId = adminId;
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
