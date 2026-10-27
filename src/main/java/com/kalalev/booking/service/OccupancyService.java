package com.kalalev.booking.service;

import com.kalalev.booking.domain.BookingType;
import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.Facility;
import com.kalalev.booking.domain.Sport;
import com.kalalev.booking.repository.BookingRepository;
import com.kalalev.booking.repository.CourtRepository;
import com.kalalev.booking.repository.FacilityRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** What managers see: each court's bookings for a day and how full the facility is. */
@Service
public class OccupancyService {

    public record FacilitySummary(long id, String name, String city) {}

    public record CourtDay(long courtId, String courtName, Sport sport, List<BookingView> bookings) {}

    /** {@code occupancyPercent} counts customer bookings only; maintenance blocks are not paid hours. */
    public record FacilityDay(
            long facilityId,
            String facilityName,
            LocalDate date,
            LocalTime opensAt,
            LocalTime closesAt,
            List<CourtDay> courts,
            int occupancyPercent) {}

    private final FacilityRepository facilityRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final Clock clock;

    public OccupancyService(
            FacilityRepository facilityRepository,
            CourtRepository courtRepository,
            BookingRepository bookingRepository,
            Clock clock) {
        this.facilityRepository = facilityRepository;
        this.courtRepository = courtRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    /** Admins see every facility; managers see the ones they run. */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<FacilitySummary> facilitiesFor(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Facility> facilities = admin
                ? facilityRepository.findAllByOrderByCityAscNameAsc()
                : facilityRepository.findByManagerEmailOrderByName(authentication.getName());
        return facilities.stream()
                .map(f -> new FacilitySummary(f.getId(), f.getName(), f.getCity()))
                .toList();
    }

    @PreAuthorize("@facilityAccess.canManageFacility(#facilityId, authentication)")
    @Transactional(readOnly = true)
    public FacilityDay day(long facilityId, LocalDate date) {
        Facility facility =
                facilityRepository.findById(facilityId).orElseThrow(() -> new NotFoundException("No facility"));
        List<Court> courts = courtRepository.findByFacility(facilityId);
        Instant from =
                date.atTime(facility.getOpensAt()).atZone(facility.getZone()).toInstant();
        Instant to =
                date.atTime(facility.getClosesAt()).atZone(facility.getZone()).toInstant();

        Map<Long, List<BookingView>> byCourt = courts.isEmpty()
                ? Map.of()
                : bookingRepository
                        .findOccupying(courts.stream().map(Court::getId).toList(), from, to, clock.instant())
                        .stream()
                        .map(BookingView::of)
                        .collect(Collectors.groupingBy(BookingView::courtId));

        List<CourtDay> courtDays = courts.stream()
                .map(c ->
                        new CourtDay(c.getId(), c.getName(), c.getSport(), byCourt.getOrDefault(c.getId(), List.of())))
                .toList();

        long openMinutes = Duration.between(from, to).toMinutes() * courts.size();
        long bookedMinutes = byCourt.values().stream()
                .flatMap(List::stream)
                .filter(b -> b.type() == BookingType.CUSTOMER)
                .mapToLong(b ->
                        Duration.between(max(b.start(), from), min(b.end(), to)).toMinutes())
                .sum();
        int percent = openMinutes == 0 ? 0 : (int) Math.round(100.0 * bookedMinutes / openMinutes);

        return new FacilityDay(
                facility.getId(),
                facility.getName(),
                date,
                facility.getOpensAt(),
                facility.getClosesAt(),
                courtDays,
                percent);
    }

    private static Instant max(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    private static Instant min(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }
}
