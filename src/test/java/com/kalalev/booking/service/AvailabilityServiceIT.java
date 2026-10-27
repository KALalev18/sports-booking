package com.kalalev.booking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kalalev.booking.IntegrationTest;
import com.kalalev.booking.MutableClock;
import com.kalalev.booking.TestData;
import com.kalalev.booking.TestcontainersConfig;
import com.kalalev.booking.domain.Sport;
import com.kalalev.booking.service.AvailabilityService.CourtAvailability;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class AvailabilityServiceIT {
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);
    private static final LocalDate TOMORROW = LocalDate.of(2026, 6, 2);

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TestData data;

    @Autowired
    private MutableClock clock;

    private long tennisCourt;

    @BeforeEach
    void setUp() {
        data.reset();
        clock.set(TestcontainersConfig.TEST_START);
        data.customer("ana@example.com");
        long varna = data.facility("Sea Garden Courts", "Varna", null);
        tennisCourt = data.court(varna, "Court 1", "TENNIS", "20.00");
        data.court(varna, "Pitch A", "FOOTBALL", "60.00");
        long sofia = data.facility("National Sports Base", "Sofia", null);
        data.court(sofia, "Hall 1", "TENNIS", "25.00");
    }

    @Test
    void listsEveryHourOfTheDayWhenNothingIsBooked() {
        List<CourtAvailability> result = availabilityService.search("Varna", Sport.TENNIS, TOMORROW);

        assertThat(result).hasSize(1);
        // Open 08:00-22:00, so the last one-hour slot starts at 21:00
        assertThat(result.get(0).freeStarts())
                .hasSize(14)
                .startsWith(LocalTime.of(8, 0))
                .endsWith(LocalTime.of(21, 0));
    }

    @Test
    void leavesOutBookedAndHeldHours() {
        bookingService.hold("ana@example.com", tennisCourt, TOMORROW, LocalTime.of(18, 0), 2);

        List<LocalTime> free = availabilityService
                .search("Varna", Sport.TENNIS, TOMORROW)
                .get(0)
                .freeStarts();
        assertThat(free)
                .doesNotContain(LocalTime.of(18, 0), LocalTime.of(19, 0))
                .contains(LocalTime.of(17, 0), LocalTime.of(20, 0));
    }

    @Test
    void showsAHoldThatRanOutAsFreeAgain() {
        bookingService.hold("ana@example.com", tennisCourt, TOMORROW, LocalTime.of(18, 0), 1);
        clock.advance(Duration.ofMinutes(10));

        assertThat(availabilityService
                        .search("Varna", Sport.TENNIS, TOMORROW)
                        .get(0)
                        .freeStarts())
                .contains(LocalTime.of(18, 0));
    }

    @Test
    void leavesOutHoursThatHaveAlreadyStartedToday() {
        // The test clock is 09:00 in Sofia, so the 09:00 slot has started
        List<LocalTime> free =
                availabilityService.search("Varna", Sport.TENNIS, TODAY).get(0).freeStarts();

        assertThat(free).first().isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void filtersByCityAndSportAndTreatsBlankAsAny() {
        assertThat(availabilityService.search(" ", Sport.TENNIS, TOMORROW)).hasSize(2);
        assertThat(availabilityService.search("varna", null, TOMORROW)).hasSize(2);
        assertThat(availabilityService.search("Plovdiv", null, TOMORROW)).isEmpty();
    }
}
