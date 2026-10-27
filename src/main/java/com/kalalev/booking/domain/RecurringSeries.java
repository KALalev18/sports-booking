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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "recurring_series")
public class RecurringSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(name = "first_date", nullable = false)
    private LocalDate firstDate;

    @Column(name = "start_local", nullable = false)
    private LocalTime startLocal;

    @Column(nullable = false)
    private short hours;

    @Column(nullable = false)
    private short weeks;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RecurringSeries() {}

    public RecurringSeries(
            AppUser user, Court court, LocalDate firstDate, LocalTime startLocal, int hours, int weeks, Instant now) {
        this.user = user;
        this.court = court;
        this.firstDate = firstDate;
        this.startLocal = startLocal;
        this.hours = (short) hours;
        this.weeks = (short) weeks;
        this.createdAt = now;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Court getCourt() {
        return court;
    }

    public LocalDate getFirstDate() {
        return firstDate;
    }

    public LocalTime getStartLocal() {
        return startLocal;
    }

    public int getHours() {
        return hours;
    }

    public int getWeeks() {
        return weeks;
    }
}
