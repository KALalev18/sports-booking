package com.kalalev.sports.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// A request to build a new facility or rebuild an existing one
@Entity
@Table(name = "Constructions")
public class Construction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConstructionId")
    private int constructionId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "FirstName", length = 100, nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "LastName", length = 100, nullable = false)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^[+0-9 ()-]{6,20}$", message = "Enter a valid phone number")
    @Column(name = "PhoneNumber", length = 100, nullable = false)
    private String phoneNumber;

    @NotBlank
    @Size(max = 250)
    @Column(name = "Description", length = 250, nullable = false)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    // Set when the request is to rebuild a facility that is already listed; null for a new facility
    @ManyToOne
    @JoinColumn(name = "FieldId")
    private Field field;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    public Construction() {}

    public boolean isReconstruction() {
        return field != null;
    }

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
