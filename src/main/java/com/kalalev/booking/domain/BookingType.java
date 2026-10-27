package com.kalalev.booking.domain;

public enum BookingType {
    /** A customer playing on the court. */
    CUSTOMER,
    /**
     * A manager closing the court, for example to repaint lines. Stored as a booking so the same database
     * constraint stops customers from booking into it.
     */
    MAINTENANCE
}
