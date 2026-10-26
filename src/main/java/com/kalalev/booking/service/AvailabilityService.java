package com.kalalev.booking.service;

import com.kalalev.booking.domain.Booking;
import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.Facility;
import com.kalalev.booking.domain.Sport;
import com.kalalev.booking.domain.TimeSlot;
import com.kalalev.booking.repository.BookingRepository;
import com.kalalev.booking.repository.CourtRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Finds the free one-hour slots on each court for a day. */
@Service
public class AvailabilityService {

    public record CourtAvailability(
            long courtId,
            String courtName,
            Sport sport,
            BigDecimal pricePerHour,
            long facilityId,
            String facilityName,
            String city,
            String address,
            List<LocalTime> freeStarts) {

        static CourtAvailability of(Court court, List<LocalTime> freeStarts) {
            Facility facility = court.getFacility();
            return new CourtAvailability(
                    court.getId(),
                    court.getName(),
                    court.getSport(),
                    court.getPricePerHour(),
                    facility.getId(),
                    facility.getName(),
                    facility.getCity(),
                    facility.getAddress(),
                    freeStarts);
        }
    }

    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final Clock clock;

    public AvailabilityService(CourtRepository courtRepository, BookingRepository bookingRepository, Clock clock) {
        this.courtRepository = courtRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    /**
     * Free slots on {@code date} for courts in {@code city} offering {@code sport} (either may be null for "any").
     * Uses two queries however many courts match: one for the courts and one for the bookings that occupy them.
     */
    @Transactional(readOnly = true)
    public List<CourtAvailability> search(String city, Sport sport, LocalDate date) {
        List<Court> courts = courtRepository.search(blankToNull(city), sport);
        if (courts.isEmpty()) {
            return List.of();
        }
        Instant now = clock.instant();
        Instant from = courts.stream()
                .map(c -> dayStart(c.getFacility(), date))
                .min(Comparator.naturalOrder())
                .get();
        Instant to = courts.stream()
                .map(c -> dayEnd(c.getFacility(), date))
                .max(Comparator.naturalOrder())
                .get();

        Map<Long, List<TimeSlot>> occupiedByCourt =
                bookingRepository
                        .findOccupying(courts.stream().map(Court::getId).toList(), from, to, now)
                        .stream()
                        .collect(Collectors.groupingBy(
                                b -> b.getCourt().getId(), Collectors.mapping(Booking::getSlot, Collectors.toList())));

        List<CourtAvailability> result = new ArrayList<>();
        for (Court court : courts) {
            List<TimeSlot> occupied = occupiedByCourt.getOrDefault(court.getId(), List.of());
            result.add(CourtAvailability.of(court, freeStarts(court.getFacility(), date, occupied, now)));
        }
        return result;
    }

    /** One-hour slots from opening until the last full hour before closing that are in the future and not taken. */
    static List<LocalTime> freeStarts(Facility facility, LocalDate date, List<TimeSlot> occupied, Instant now) {
        List<LocalTime> free = new ArrayList<>();
        LocalTime start = facility.getOpensAt();
        LocalTime end = start.plusHours(1);
        while (end.isAfter(start) && !end.isAfter(facility.getClosesAt())) {
            Instant slotStart = date.atTime(start).atZone(facility.getZone()).toInstant();
            TimeSlot slot = new TimeSlot(
                    slotStart, date.atTime(end).atZone(facility.getZone()).toInstant());
            if (slot.start().isAfter(now) && occupied.stream().noneMatch(slot::overlaps)) {
                free.add(start);
            }
            start = end;
            end = start.plusHours(1);
        }
        return free;
    }

    private static Instant dayStart(Facility facility, LocalDate date) {
        return date.atTime(facility.getOpensAt()).atZone(facility.getZone()).toInstant();
    }

    private static Instant dayEnd(Facility facility, LocalDate date) {
        return date.atTime(facility.getClosesAt()).atZone(facility.getZone()).toInstant();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
