package com.kalalev.booking.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ClockConfig {

    // Every time-based rule reads the time from this clock, so tests can fix or move it
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
