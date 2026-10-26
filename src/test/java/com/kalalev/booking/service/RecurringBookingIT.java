package com.kalalev.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kalalev.booking.IntegrationTest;
import com.kalalev.booking.MutableClock;
import com.kalalev.booking.TestData;
import com.kalalev.booking.TestcontainersConfig;
import com.kalalev.booking.service.RecurringBookingService.Occurrence;
import com.kalalev.booking.service.RecurringBookingService.Outcome;
import com.kalalev.booking.service.RecurringBookingService.Result;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class RecurringBookingIT {
    // Tuesdays, starting the day after the test clock
    private static final LocalDate FIRST_TUESDAY = LocalDate.of(2026, 6, 2);
    private static final LocalTime SIX_PM = LocalTime.of(18, 0);

    @Autowired
    private RecurringBookingService recurringBookingService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TestData data;

    @Autowired
    private MutableClock clock;

    private long court;

    @BeforeEach
    void setUp() {
        data.reset();
        clock.set(TestcontainersConfig.TEST_START);
        data.customer("team@example.com");
        data.customer("ana@example.com");
        court = data.court(data.facility("Arena", "Sofia", null), "Pitch A", "FOOTBALL", "60.00");
    }

    @Test
    void booksEveryWeekWhenAllAreFree() {
        Result result = recurringBookingService.book("team@example.com", court, FIRST_TUESDAY, SIX_PM, 2, 10);

        assertThat(result.booked()).isEqualTo(10);
        assertThat(result.occurrences())
                .extracting(Occurrence::date)
                .startsWith(FIRST_TUESDAY, FIRST_TUESDAY.plusWeeks(1));
        assertThat(data.activeBookingsOn(court)).isEqualTo(10);
    }

    @Test
    void reportsClashingWeeksAndStillBooksTheRest() {
        LocalDate thirdWeek = FIRST_TUESDAY.plusWeeks(2);
        bookingService.hold("ana@example.com", court, thirdWeek, LocalTime.of(19, 0), 1);

        Result result = recurringBookingService.book("team@example.com", court, FIRST_TUESDAY, SIX_PM, 2, 4);

        assertThat(result.booked()).isEqualTo(3);
        assertThat(result.occurrences())
                .filteredOn(o -> o.outcome() == Outcome.CONFLICT)
                .singleElement()
                .extracting(Occurrence::date)
                .isEqualTo(thirdWeek);
    }

    @Test
    void rejectsSeriesThatAreTooShortOrTooLong() {
        assertThatThrownBy(() -> recurringBookingService.book("team@example.com", court, FIRST_TUESDAY, SIX_PM, 1, 1))
                .isInstanceOf(BookingException.class);
        assertThatThrownBy(() -> recurringBookingService.book("team@example.com", court, FIRST_TUESDAY, SIX_PM, 1, 13))
                .isInstanceOf(BookingException.class);
    }
}
