package com.kalalev.booking.service;

import com.kalalev.booking.repository.BookingRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marks holds that were never confirmed as EXPIRED, once a minute. Search already ignores run-out holds and a new
 * booking releases them itself, so this job keeps the table tidy rather than being needed for correctness.
 */
@Component
@ConditionalOnProperty(name = "booking.expiry-job.enabled", havingValue = "true", matchIfMissing = true)
public class HoldExpiryJob {
    private static final Logger log = LoggerFactory.getLogger(HoldExpiryJob.class);

    private final BookingRepository bookingRepository;
    private final Clock clock;
    private final Counter expiredHolds;

    public HoldExpiryJob(BookingRepository bookingRepository, Clock clock, MeterRegistry meterRegistry) {
        this.bookingRepository = bookingRepository;
        this.clock = clock;
        this.expiredHolds = Counter.builder("booking.holds.expired")
                .description("Holds released because they were not confirmed in time")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${booking.expiry-job.interval:PT1M}")
    @Transactional
    public int expireRunOutHolds() {
        int expired = bookingRepository.expireAllRunOutHolds(clock.instant());
        if (expired > 0) {
            expiredHolds.increment(expired);
            log.info("Released {} unconfirmed hold(s)", expired);
        }
        return expired;
    }
}
