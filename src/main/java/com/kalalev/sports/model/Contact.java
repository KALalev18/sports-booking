package com.kalalev.sports.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ContactId", columnDefinition = "int", nullable = false)
    private int contactId;
    @Column(name = "FirstName", columnDefinition = "nvarchar(100)", nullable = false)
    private String firstName;
    @Column(name = "LastName", columnDefinition = "nvarchar(100)", nullable = false)
    private String lastName;
    @Column(name = "PhoneNumber", columnDefinition = "nvarchar(100)", nullable = false)
    private String phoneNumber;
    @Column(name = "Subject", columnDefinition = "varchar(250)", nullable = false)
    private String subject;
    @OneToOne
    @JoinColumn(name = "UserId")
    @Column(name = "UserId", columnDefinition = "int", nullable = false)
    private User userId;

    public Contact(int contactId, String firstName, String lastName, String phoneNumber, String subject, User userId) {
        this.contactId = contactId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.subject = subject;
        this.userId = userId;
    }
    public Contact(){}

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }
}
