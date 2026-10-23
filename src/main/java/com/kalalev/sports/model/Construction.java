package com.kalalev.sports.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Constructions")
public class Construction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConstructionId", columnDefinition = "int", nullable = false)
    private int constructionId;
    @Column(name = "FirstName", columnDefinition = "nvarchar(50)", nullable = false)
    private String firstName;
    @Column(name = "LastName", columnDefinition = "nvarchar(50)", nullable = false)
    private String lastName;
    @Column(name = "PhoneNumber", columnDefinition = "nvarchar(50)", nullable = false)
    private String phoneNumber;
    @Column(name = "Description", columnDefinition = "varchar(250)", nullable = false)
    private String description;
    @OneToOne
    @JoinColumn(name = "UserId")
    @Column(name = "UserId", columnDefinition = "int", nullable = false)
    private User userId;

    public Construction(int constructionId, String firstName, String lastName, String phoneNumber, String description, User userId) {
        this.constructionId = constructionId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.userId = userId;
    }

    public Construction(){}

    public int getConstructionId() {
        return constructionId;
    }

    public void setConstructionId(int constructionId) {
        this.constructionId = constructionId;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }
}
