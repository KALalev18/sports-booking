package com.kalalev.booking.domain;

import java.time.Instant;

/** A half-open time range [start, end), matching the '[)' ranges in the database constraint. */
public record TimeSlot(Instant start, Instant end) {
    public TimeSlot {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
    }

    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
