package com.kalalev.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** One bookable pitch or court inside a facility. */
@Entity
@Table(name = "court")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sport sport;

    @Column(name = "price_per_hour", nullable = false, precision = 8, scale = 2)
    private BigDecimal pricePerHour;

    protected Court() {}

    public Court(Facility facility, String name, Sport sport, BigDecimal pricePerHour) {
        this.facility = facility;
        this.name = name;
        this.sport = sport;
        this.pricePerHour = pricePerHour;
    }

    public Long getId() {
        return id;
    }

    public Facility getFacility() {
        return facility;
    }

    public String getName() {
        return name;
    }

    public Sport getSport() {
        return sport;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }
}
