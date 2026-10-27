package com.kalalev.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.time.ZoneId;

@Entity
@Table(name = "facility")
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 60)
    private String city;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(name = "time_zone", nullable = false, length = 40)
    private String timeZone;

    @Column(name = "opens_at", nullable = false)
    private LocalTime opensAt;

    @Column(name = "closes_at", nullable = false)
    private LocalTime closesAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private AppUser manager;

    protected Facility() {}

    public Facility(
            String name,
            String city,
            String address,
            ZoneId timeZone,
            LocalTime opensAt,
            LocalTime closesAt,
            AppUser manager) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.timeZone = timeZone.getId();
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.manager = manager;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public ZoneId getZone() {
        return ZoneId.of(timeZone);
    }

    public LocalTime getOpensAt() {
        return opensAt;
    }

    public LocalTime getClosesAt() {
        return closesAt;
    }

    public AppUser getManager() {
        return manager;
    }
}
