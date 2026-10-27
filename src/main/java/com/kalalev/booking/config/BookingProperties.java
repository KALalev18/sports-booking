package com.kalalev.booking.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Booking rules that a facility owner might want to change, set under {@code booking.*}.
 *
 * @param holdDuration how long a held slot waits for confirmation before it is released
 * @param freeCancellationWindow customers can cancel a confirmed booking until this long before it starts
 * @param maxHours longest booking a customer can make in one go
 * @param maxActiveHolds how many slots one customer can hold at once, so nobody can lock up every court
 */
@ConfigurationProperties("booking")
public record BookingProperties(
        @DefaultValue("10m") Duration holdDuration,
        @DefaultValue("24h") Duration freeCancellationWindow,
        @DefaultValue("3") int maxHours,
        @DefaultValue("3") int maxActiveHolds) {}
