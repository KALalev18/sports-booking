package com.kalalev.booking.service;

import com.kalalev.booking.domain.Booking;
import com.kalalev.booking.domain.BookingStatus;
import com.kalalev.booking.domain.BookingType;
import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.Facility;
import com.kalalev.booking.domain.Sport;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * A read-only copy of a booking, built inside the service's transaction. Controllers and templates get this rather
 * than the JPA entity, so nothing outside the service can trigger lazy loading or change a managed entity.
 */
public record BookingView(
        long id,
        BookingStatus status,
        BookingType type,
        long courtId,
        String courtName,
        Sport sport,
        long facilityId,
        String facilityName,
        String city,
        ZoneId zone,
        Instant start,
        Instant end,
        BigDecimal price,
        Instant holdExpiresAt,
        String customerName,
        String customerEmail,
        String note,
        Long seriesId) {

    public static BookingView of(Booking booking) {
        Court court = booking.getCourt();
        Facility facility = court.getFacility();
        return new BookingView(
                booking.getId(),
                booking.getStatus(),
                booking.getType(),
                court.getId(),
                court.getName(),
                court.getSport(),
                facility.getId(),
                facility.getName(),
                facility.getCity(),
                facility.getZone(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPrice(),
                booking.getHoldExpiresAt(),
                booking.getUser().getFullName(),
                booking.getUser().getEmail(),
                booking.getNote(),
                booking.getSeries() == null ? null : booking.getSeries().getId());
    }

    /** Start time at the facility, for display. */
    public ZonedDateTime localStart() {
        return start.atZone(zone);
    }

    public ZonedDateTime localEnd() {
        return end.atZone(zone);
    }

    public boolean active() {
        return status == BookingStatus.HELD || status == BookingStatus.CONFIRMED;
    }
}
