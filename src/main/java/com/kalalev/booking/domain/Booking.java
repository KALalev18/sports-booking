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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private BookingType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BookingStatus status;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;

    @Column(length = 200)
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Booking() {}

    private Booking(
            Court court,
            AppUser user,
            BookingType type,
            BookingStatus status,
            TimeSlot slot,
            BigDecimal price,
            Instant holdExpiresAt,
            String note,
            Instant now) {
        this.court = court;
        this.user = user;
        this.type = type;
        this.status = status;
        this.startTime = slot.start();
        this.endTime = slot.end();
        this.price = price;
        this.holdExpiresAt = holdExpiresAt;
        this.note = note;
        this.createdAt = now;
    }

    /** Reserves the slot for a few minutes while the customer checks out. */
    public static Booking hold(
            Court court, AppUser user, TimeSlot slot, BigDecimal price, Instant expiresAt, Instant now) {
        return new Booking(court, user, BookingType.CUSTOMER, BookingStatus.HELD, slot, price, expiresAt, null, now);
    }

    /** Books a slot straight away, without a hold; used for recurring bookings. */
    public static Booking confirmed(Court court, AppUser user, TimeSlot slot, BigDecimal price, Instant now) {
        return new Booking(court, user, BookingType.CUSTOMER, BookingStatus.CONFIRMED, slot, price, null, null, now);
    }

    public static Booking maintenance(Court court, AppUser manager, TimeSlot slot, String note, Instant now) {
        return new Booking(
                court,
                manager,
                BookingType.MAINTENANCE,
                BookingStatus.CONFIRMED,
                slot,
                BigDecimal.ZERO,
                null,
                note,
                now);
    }

    public boolean isHoldExpired(Instant now) {
        return status == BookingStatus.HELD && !holdExpiresAt.isAfter(now);
    }

    public void confirm() {
        status = BookingStatus.CONFIRMED;
        holdExpiresAt = null;
    }

    public void cancel() {
        status = BookingStatus.CANCELLED;
        holdExpiresAt = null;
    }

    public boolean isActive() {
        return status == BookingStatus.HELD || status == BookingStatus.CONFIRMED;
    }

    public TimeSlot getSlot() {
        return new TimeSlot(startTime, endTime);
    }

    public Long getId() {
        return id;
    }

    public Court getCourt() {
        return court;
    }

    public AppUser getUser() {
        return user;
    }

    public BookingType getType() {
        return type;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getVersion() {
        return version;
    }
}
