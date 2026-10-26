package com.kalalev.booking.repository;

import com.kalalev.booking.domain.RecurringSeries;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringSeriesRepository extends JpaRepository<RecurringSeries, Long> {}
