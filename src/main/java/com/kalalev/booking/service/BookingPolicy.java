package com.kalalev.booking.service;

import static com.kalalev.booking.service.BookingException.Reason.INVALID_TIME;

import com.kalalev.booking.config.BookingProperties;
import com.kalalev.booking.domain.Booking;
import com.kalalev.booking.domain.BookingStatus;
import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.Facility;
import com.kalalev.booking.domain.TimeSlot;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

/** The time and price rules for bookings, with no database access so they are easy to unit test. */
@Component
public class BookingPolicy {
    private final BookingProperties properties;
    private final Clock clock;

    public BookingPolicy(BookingProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Turns a local date and start time at the court's facility into a UTC time slot, checking that it starts on
     * the hour, lies inside opening hours, is not too long and has not already started.
     */
    public TimeSlot slotFor(Court court, LocalDate date, LocalTime start, int hours, int maxHours) {
        if (hours < 1 || hours > maxHours) {
            throw new BookingException(INVALID_TIME, "A booking must be 1 to " + maxHours + " hours long");
        }
        if (start.getMinute() != 0 || start.getSecond() != 0 || start.getNano() != 0) {
            throw new BookingException(INVALID_TIME, "Bookings start on the hour");
        }
        Facility facility = court.getFacility();
        LocalTime end = start.plusHours(hours);
        boolean wrapsMidnight = !end.isAfter(start);
        if (start.isBefore(facility.getOpensAt()) || wrapsMidnight || end.isAfter(facility.getClosesAt())) {
            throw new BookingException(
                    INVALID_TIME,
                    "%s is open %s–%s".formatted(facility.getName(), facility.getOpensAt(), facility.getClosesAt()));
        }
        TimeSlot slot = new TimeSlot(
                date.atTime(start).atZone(facility.getZone()).toInstant(),
                date.atTime(end).atZone(facility.getZone()).toInstant());
        if (!slot.start().isAfter(clock.instant())) {
            throw new BookingException(INVALID_TIME, "That time has already started");
        }
        return slot;
    }

    public TimeSlot customerSlot(Court court, LocalDate date, LocalTime start, int hours) {
        return slotFor(court, date, start, hours, properties.maxHours());
    }

    public BigDecimal price(Court court, TimeSlot slot) {
        long hours = Duration.between(slot.start(), slot.end()).toHours();
        return court.getPricePerHour().multiply(BigDecimal.valueOf(hours));
    }

    public Instant holdExpiry() {
        return clock.instant().plus(properties.holdDuration());
    }

    /** Holds can always be released; confirmed bookings only until the free cancellation window closes. */
    public boolean customerCanCancel(Booking booking) {
        if (booking.getStatus() == BookingStatus.HELD) {
            return true;
        }
        Instant lastFreeCancellation = booking.getStartTime().minus(properties.freeCancellationWindow());
        return !clock.instant().isAfter(lastFreeCancellation);
    }

    public long freeCancellationHours() {
        return properties.freeCancellationWindow().toHours();
    }

    public int maxActiveHolds() {
        return properties.maxActiveHolds();
    }

    public Instant now() {
        return clock.instant();
    }
}
