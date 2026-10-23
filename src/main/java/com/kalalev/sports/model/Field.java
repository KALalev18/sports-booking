package com.kalalev.sports.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

// A sports facility listed on the Facilities page
@Entity
@Table(name = "Fields")
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FieldId")
    private int fieldId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "FieldName", length = 100, nullable = false)
    private String fieldName;

    // City the facility is in
    @NotBlank
    @Size(max = 100)
    @Column(name = "FieldLocation", length = 100, nullable = false)
    private String fieldLocation;

    @NotBlank
    @Size(max = 50)
    @Column(name = "WorkingDays", length = 50, nullable = false)
    private String workingDays;

    @NotNull
    @DateTimeFormat(pattern = "HH:mm")
    @Column(name = "StartedWorkingTime", nullable = false)
    private LocalTime startedWorkingTime;

    @NotNull
    @DateTimeFormat(pattern = "HH:mm")
    @Column(name = "FinishedWorkingTime", nullable = false)
    private LocalTime finishedWorkingTime;

    // Null means the facility is free to use
    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "PriceByHour", precision = 10, scale = 2)
    private BigDecimal priceByHour;

    @Size(max = 500)
    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    // Google Maps embed URL
    @Size(max = 1000)
    @Column(name = "MapUrl", length = 1000)
    private String mapUrl;

    public Field() {}

    public Field(
            String fieldName,
            String fieldLocation,
            String workingDays,
            LocalTime startedWorkingTime,
            LocalTime finishedWorkingTime,
            BigDecimal priceByHour,
            String imageUrl,
            String mapUrl) {
        this.fieldName = fieldName;
        this.fieldLocation = fieldLocation;
        this.workingDays = workingDays;
        this.startedWorkingTime = startedWorkingTime;
        this.finishedWorkingTime = finishedWorkingTime;
        this.priceByHour = priceByHour;
        this.imageUrl = imageUrl;
        this.mapUrl = mapUrl;
    }

    @AssertTrue(message = "Closing time must be after opening time")
    public boolean isWorkingTimeValid() {
        return startedWorkingTime == null
                || finishedWorkingTime == null
                || finishedWorkingTime.isAfter(startedWorkingTime);
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldLocation() {
        return fieldLocation;
    }

    public void setFieldLocation(String fieldLocation) {
        this.fieldLocation = fieldLocation;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }

    public LocalTime getStartedWorkingTime() {
        return startedWorkingTime;
    }

    public void setStartedWorkingTime(LocalTime startedWorkingTime) {
        this.startedWorkingTime = startedWorkingTime;
    }

    public LocalTime getFinishedWorkingTime() {
        return finishedWorkingTime;
    }

    public void setFinishedWorkingTime(LocalTime finishedWorkingTime) {
        this.finishedWorkingTime = finishedWorkingTime;
    }

    public BigDecimal getPriceByHour() {
        return priceByHour;
    }

    public void setPriceByHour(BigDecimal priceByHour) {
        this.priceByHour = priceByHour;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }
}
