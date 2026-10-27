package com.kalalev.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kalalev.booking.IntegrationTest;
import com.kalalev.booking.MutableClock;
import com.kalalev.booking.TestData;
import com.kalalev.booking.TestcontainersConfig;
import com.kalalev.booking.domain.Booking;
import com.kalalev.booking.domain.BookingStatus;
import com.kalalev.booking.repository.BookingRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@IntegrationTest
@Import(HoldExpiryJobIT.JobConfig.class)
class HoldExpiryJobIT {
    private static final LocalDate TOMORROW = LocalDate.of(2026, 6, 2);

    /** The job is switched off for integration tests, so this test creates one it can call directly. */
    @TestConfiguration(proxyBeanMethods = false)
    static class JobConfig {
        @Bean
        HoldExpiryJob manualHoldExpiryJob(BookingRepository bookingRepository, Clock clock, MeterRegistry registry) {
            return new HoldExpiryJob(bookingRepository, clock, registry);
        }
    }

    @Autowired
    private HoldExpiryJob job;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestData data;

    @Autowired
    private MutableClock clock;

    private long court;

    @BeforeEach
    void setUp() {
        data.reset();
        clock.set(TestcontainersConfig.TEST_START);
        data.customer("ana@example.com");
        court = data.court(data.facility("Arena", "Sofia", null), "Court 1", "TENNIS", "20.00");
    }

    @Test
    void releasesOnlyHoldsWhoseTimeRanOut() {
        BookingView old = bookingService.hold("ana@example.com", court, TOMORROW, LocalTime.of(10, 0), 1);
        clock.advance(Duration.ofMinutes(6));
        BookingView fresh = bookingService.hold("ana@example.com", court, TOMORROW, LocalTime.of(11, 0), 1);
        clock.advance(Duration.ofMinutes(5));

        assertThat(job.expireRunOutHolds()).isEqualTo(1);
        assertThat(bookingRepository.findById(old.id()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.EXPIRED);
        assertThat(bookingRepository.findById(fresh.id()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.HELD);
    }

    @Test
    void aConfirmBasedOnAStaleReadLosesToTheJob() {
        BookingView hold = bookingService.hold("ana@example.com", court, TOMORROW, LocalTime.of(10, 0), 1);
        // A copy read before the job ran, as a slow confirm request would have it
        Booking staleCopy = bookingRepository.findById(hold.id()).orElseThrow();

        clock.advance(Duration.ofMinutes(10));
        job.expireRunOutHolds();

        staleCopy.confirm();
        assertThatThrownBy(() -> bookingRepository.saveAndFlush(staleCopy))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(bookingRepository.findById(hold.id()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.EXPIRED);
    }
}
