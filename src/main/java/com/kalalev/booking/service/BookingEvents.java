package com.kalalev.booking.service;

/** Events the booking service publishes; {@link NotificationService} turns them into emails. */
public final class BookingEvents {

    public record Confirmed(BookingView booking) {}

    /** {@code byFacility} is true when a manager or admin cancelled it, not the customer. */
    public record Cancelled(BookingView booking, boolean byFacility) {}

    private BookingEvents() {}
}
