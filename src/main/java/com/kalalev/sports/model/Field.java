package com.kalalev.sports.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Fields")
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FieldId", columnDefinition = "int", nullable = false)
    private int fieldId;
    @Column(name = "FieldLocation", columnDefinition = "nvarchar(100)", nullable = false)
    private int fieldLocation;
    @Column(name = "StartedWorkingTime", columnDefinition = "time(7)", nullable = false)
    private String startedWorkingTime;
    @Column(name = "FinishedWorkingTime", columnDefinition = "time(7)", nullable = false)
    private String finishedWorkingTime;
    @Column(name = "PriceByHour", columnDefinition = "decimal(10, 2)", nullable = true)
    private float priceByHour;

    public Field(int fieldId, int fieldLocation, String startedWorkingTime, String finishedWorkingTime, float priceByHour) {
        this.fieldId = fieldId;
        this.fieldLocation = fieldLocation;
        this.startedWorkingTime = startedWorkingTime;
        this.finishedWorkingTime = finishedWorkingTime;
        this.priceByHour = priceByHour;
    }

    public Field() {

    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public int getFieldLocation(String pomorie) {
        return fieldLocation;
    }

    public void setFieldLocation(int fieldLocation) {
        this.fieldLocation = fieldLocation;
    }

    public String getStartedWorkingTime(String s) {
        return startedWorkingTime;
    }

    public void setStartedWorkingTime(String startedWorkingTime) {
        this.startedWorkingTime = startedWorkingTime;
    }

    public String getFinishedWorkingTime(String s) {
        return finishedWorkingTime;
    }

    public void setFinishedWorkingTime(String finishedWorkingTime) {
        this.finishedWorkingTime = finishedWorkingTime;
    }

    public float getPriceByHour(int i) {
        return priceByHour;
    }

    public void setPriceByHour(float priceByHour) {
        this.priceByHour = priceByHour;
    }
}
