package com.kalalev.booking.repository;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kalalev.booking.IntegrationTest;
import com.kalalev.booking.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Tests the booking_no_overlap constraint directly with SQL, bypassing the Java code, to show the database itself
 * refuses double bookings.
 */
@IntegrationTest
class ExclusionConstraintIT {
    @Autowired
    private TestData data;

    private long court;
    private long otherCourt;
    private long user;

    @BeforeEach
    void setUp() {
        data.reset();
        user = data.customer("ana@example.com");
        long facility = data.facility("Arena", "Sofia", null);
        court = data.court(facility, "Court 1", "TENNIS", "20.00");
        otherCourt = data.court(facility, "Court 2", "TENNIS", "20.00");
    }

    private void insert(long courtId, String status, String start, String end) {
        data.jdbc().update("""
                        INSERT INTO booking (court_id, user_id, type, status, start_time, end_time, price,
                                             hold_expires_at, created_at)
                        VALUES (?, ?, 'CUSTOMER', ?, ?::timestamptz, ?::timestamptz, 20,
                                CASE WHEN ? = 'HELD' THEN now() + interval '10 minutes' END, now())
                        """, courtId, user, status, start, end, status);
    }

    @Test
    void rejectsOverlappingActiveBookingsOnTheSameCourt() {
        insert(court, "CONFIRMED", "2026-06-02 15:00Z", "2026-06-02 17:00Z");

        assertThatThrownBy(() -> insert(court, "HELD", "2026-06-02 16:00Z", "2026-06-02 18:00Z"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("booking_no_overlap");
    }

    @Test
    void allowsBackToBackBookings() {
        insert(court, "CONFIRMED", "2026-06-02 15:00Z", "2026-06-02 16:00Z");

        assertThatCode(() -> insert(court, "CONFIRMED", "2026-06-02 16:00Z", "2026-06-02 17:00Z"))
                .doesNotThrowAnyException();
    }

    @Test
    void allowsTheSameTimeOnAnotherCourt() {
        insert(court, "CONFIRMED", "2026-06-02 15:00Z", "2026-06-02 16:00Z");

        assertThatCode(() -> insert(otherCourt, "CONFIRMED", "2026-06-02 15:00Z", "2026-06-02 16:00Z"))
                .doesNotThrowAnyException();
    }

    @Test
    void ignoresCancelledAndExpiredBookings() {
        insert(court, "CANCELLED", "2026-06-02 15:00Z", "2026-06-02 16:00Z");
        insert(court, "EXPIRED", "2026-06-02 15:00Z", "2026-06-02 16:00Z");

        assertThatCode(() -> insert(court, "CONFIRMED", "2026-06-02 15:00Z", "2026-06-02 16:00Z"))
                .doesNotThrowAnyException();
    }
}
