package com.kalalev.booking.service;

/** A booking request that breaks a business rule. The web and API layers turn {@link Reason} into a response. */
public class BookingException extends RuntimeException {

    public enum Reason {
        /** The slot is already held or booked by someone else. */
        SLOT_UNAVAILABLE,
        /** The hold ran out before the customer confirmed it. */
        HOLD_EXPIRED,
        /** Too close to the start time for a customer to cancel. */
        CANCELLATION_WINDOW_CLOSED,
        /** The customer already holds the maximum number of slots. */
        TOO_MANY_HOLDS,
        /** The booking is not in a state that allows this action, for example confirming a cancelled booking. */
        INVALID_STATE,
        /** The requested time is not bookable: in the past, outside opening hours, too long, not on the hour. */
        INVALID_TIME
    }

    private final Reason reason;

    public BookingException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
