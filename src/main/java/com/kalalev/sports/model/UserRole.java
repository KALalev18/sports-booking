package com.kalalev.sports.model;

import jakarta.persistence.*;

@Entity
@Table(name = "UserRoles")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleId", columnDefinition = "int", nullable = false)
    private int roleId;
    @Column(name = "RoleName", columnDefinition = "varchar(10)", nullable = false)
    private String roleName;

    public UserRole(){}

    public UserRole(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName(String roleName) {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void checkRole(String roleName)
    {
        try{
            if (roleName == "User" || roleName == "Admin")
            {
                throw new Exception("User has the required role.");
            }
            System.out.println("User does not have the required role.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
