package com.kalalev.booking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kalalev.booking.IntegrationTest;
import com.kalalev.booking.TestData;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Fires many booking requests for the same slot at the same moment and checks that exactly one wins. This is the
 * case a "check, then insert" approach gets wrong: several threads see the slot as free before any of them saves.
 */
@IntegrationTest
class ConcurrentBookingIT {
    private static final int CUSTOMERS = 50;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TestData data;

    private long court;

    @BeforeEach
    void setUp() {
        data.reset();
        long facility = data.facility("Arena", "Sofia", null);
        court = data.court(facility, "Court 1", "PADEL", "30.00");
        for (int i = 0; i < CUSTOMERS; i++) {
            data.customer("customer" + i + "@example.com");
        }
    }

    @RepeatedTest(5)
    void exactlyOneOfFiftySimultaneousRequestsGetsTheSlot() throws Exception {
        CountDownLatch startTogether = new CountDownLatch(1);
        List<Future<String>> results = new ArrayList<>();

        try (ExecutorService pool = Executors.newFixedThreadPool(CUSTOMERS)) {
            for (int i = 0; i < CUSTOMERS; i++) {
                String email = "customer" + i + "@example.com";
                results.add(pool.submit(() -> {
                    startTogether.await();
                    try {
                        bookingService.hold(email, court, LocalDate.of(2026, 6, 2), LocalTime.of(18, 0), 2);
                        return "booked";
                    } catch (BookingException e) {
                        return e.getReason().name();
                    }
                }));
            }
            startTogether.countDown();

            List<String> outcomes = new ArrayList<>();
            for (Future<String> result : results) {
                outcomes.add(result.get());
            }
            Map<String, Long> counts =
                    outcomes.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            assertThat(counts).containsOnly(Map.entry("booked", 1L), Map.entry("SLOT_UNAVAILABLE", 49L));
            assertThat(data.activeBookingsOn(court)).isEqualTo(1);
        }
    }
}
