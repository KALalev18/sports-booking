package com.kalalev.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kalalev.booking.MutableClock;
import com.kalalev.booking.config.BookingProperties;
import com.kalalev.booking.domain.AppUser;
import com.kalalev.booking.domain.Booking;
import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.Facility;
import com.kalalev.booking.domain.Role;
import com.kalalev.booking.domain.Sport;
import com.kalalev.booking.domain.TimeSlot;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BookingPolicyTest {
    private static final ZoneId SOFIA = ZoneId.of("Europe/Sofia");
    // Monday 1 June 2026, 09:00 in Sofia (UTC+3 in summer)
    private final MutableClock clock = new MutableClock(Instant.parse("2026-06-01T06:00:00Z"));
    private final BookingPolicy policy =
            new BookingPolicy(new BookingProperties(Duration.ofMinutes(10), Duration.ofHours(24), 3, 3), clock);

    private final Facility facility =
            new Facility("Test Arena", "Sofia", "Street 1", SOFIA, LocalTime.of(8, 0), LocalTime.of(22, 0), null);
    private final Court court = new Court(facility, "Court 1", Sport.TENNIS, new BigDecimal("20.00"));
    private final LocalDate tomorrow = LocalDate.of(2026, 6, 2);

    @Test
    void convertsLocalTimeAtTheFacilityToUtc() {
        TimeSlot slot = policy.customerSlot(court, tomorrow, LocalTime.of(18, 0), 2);

        assertThat(slot.start()).isEqualTo(Instant.parse("2026-06-02T15:00:00Z"));
        assertThat(slot.end()).isEqualTo(Instant.parse("2026-06-02T17:00:00Z"));
    }

    @Test
    void usesWinterTimeInWinter() {
        TimeSlot slot = policy.customerSlot(court, LocalDate.of(2026, 12, 1), LocalTime.of(18, 0), 1);

        assertThat(slot.start()).isEqualTo(Instant.parse("2026-12-01T16:00:00Z"));
    }

    @ParameterizedTest(name = "{0} for {1}h: {2}")
    @CsvSource({
        "07:00, 1, before opening",
        "21:00, 2, past closing",
        "18:30, 1, not on the hour",
        "18:00, 0, zero hours",
        "18:00, 4, longer than the maximum"
    })
    void rejectsTimesThatCannotBeBooked(LocalTime start, int hours, String why) {
        assertThatThrownBy(() -> policy.customerSlot(court, tomorrow, start, hours))
                .isInstanceOf(BookingException.class)
                .extracting(e -> ((BookingException) e).getReason())
                .isEqualTo(BookingException.Reason.INVALID_TIME);
    }

    @Test
    void rejectsASlotThatHasAlreadyStarted() {
        assertThatThrownBy(() -> policy.customerSlot(court, LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), 1))
                .hasMessage("That time has already started");
    }

    @Test
    void allowsTheLastHourBeforeClosing() {
        assertThat(policy.customerSlot(court, tomorrow, LocalTime.of(21, 0), 1).end())
                .isEqualTo(Instant.parse("2026-06-02T19:00:00Z"));
    }

    @Test
    void pricesByTheHour() {
        TimeSlot twoHours = policy.customerSlot(court, tomorrow, LocalTime.of(18, 0), 2);

        assertThat(policy.price(court, twoHours)).isEqualByComparingTo("40.00");
    }

    @Test
    void customersCanCancelUntil24HoursBefore() {
        AppUser customer = new AppUser("ana@example.com", "hash", "Ana", Role.CUSTOMER, clock.instant());
        TimeSlot slot = new TimeSlot(Instant.parse("2026-06-02T15:00:00Z"), Instant.parse("2026-06-02T16:00:00Z"));
        Booking booking = Booking.confirmed(court, customer, slot, new BigDecimal("20.00"), clock.instant());

        clock.set(Instant.parse("2026-06-01T15:00:00Z"));
        assertThat(policy.customerCanCancel(booking)).isTrue();

        clock.set(Instant.parse("2026-06-01T15:00:01Z"));
        assertThat(policy.customerCanCancel(booking)).isFalse();
    }
}
