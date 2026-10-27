package com.kalalev.booking.domain;

/**
 * HELD -> CONFIRMED -> CANCELLED, or HELD -> EXPIRED / CANCELLED. Only HELD and CONFIRMED occupy the
 * court; the database exclusion constraint ignores the other two.
 */
public enum BookingStatus {
    HELD,
    CONFIRMED,
    CANCELLED,
    EXPIRED
}
