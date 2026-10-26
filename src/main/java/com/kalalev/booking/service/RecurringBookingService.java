package com.kalalev.booking.service;

import com.kalalev.booking.domain.AppUser;
import com.kalalev.booking.domain.Booking;
import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.RecurringSeries;
import com.kalalev.booking.repository.CourtRepository;
import com.kalalev.booking.repository.RecurringSeriesRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Books the same court at the same time every week, for example a five-a-side team on Tuesdays at 18:00. Weeks that
 * clash with existing bookings are reported and skipped, and the free weeks are still booked.
 */
@Service
public class RecurringBookingService {
    public static final int MIN_WEEKS = 2;
    public static final int MAX_WEEKS = 12;

    public enum Outcome {
        BOOKED,
        CONFLICT,
        NOT_BOOKABLE
    }

    public record Occurrence(LocalDate date, Outcome outcome, String message, Long bookingId) {}

    public record Result(Long seriesId, List<Occurrence> occurrences) {
        public long booked() {
            return occurrences.stream()
                    .filter(o -> o.outcome() == Outcome.BOOKED)
                    .count();
        }
    }

    private final BookingService bookingService;
    private final UserService userService;
    private final CourtRepository courtRepository;
    private final RecurringSeriesRepository seriesRepository;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public RecurringBookingService(
            BookingService bookingService,
            UserService userService,
            CourtRepository courtRepository,
            RecurringSeriesRepository seriesRepository,
            TransactionTemplate transactionTemplate,
            Clock clock) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.courtRepository = courtRepository;
        this.seriesRepository = seriesRepository;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
    }

    /** Not transactional on purpose: each week is booked in its own transaction by {@link BookingService}. */
    public Result book(String email, long courtId, LocalDate firstDate, LocalTime start, int hours, int weeks) {
        if (weeks < MIN_WEEKS || weeks > MAX_WEEKS) {
            throw new BookingException(
                    BookingException.Reason.INVALID_TIME,
                    "A recurring booking covers " + MIN_WEEKS + " to " + MAX_WEEKS + " weeks");
        }
        RecurringSeries series = transactionTemplate.execute(status -> {
            AppUser customer = userService.getByEmail(email);
            Court court =
                    courtRepository.findById(courtId).orElseThrow(() -> new NotFoundException("No court " + courtId));
            return seriesRepository.save(
                    new RecurringSeries(customer, court, firstDate, start, hours, weeks, clock.instant()));
        });

        List<Occurrence> occurrences = new ArrayList<>();
        for (int week = 0; week < weeks; week++) {
            LocalDate date = firstDate.plusWeeks(week);
            try {
                Booking booking = bookingService.bookOccurrence(series, date);
                occurrences.add(new Occurrence(date, Outcome.BOOKED, "Booked", booking.getId()));
            } catch (BookingException e) {
                Outcome outcome = e.getReason() == BookingException.Reason.SLOT_UNAVAILABLE
                        ? Outcome.CONFLICT
                        : Outcome.NOT_BOOKABLE;
                occurrences.add(new Occurrence(date, outcome, e.getMessage(), null));
            }
        }
        return new Result(series.getId(), occurrences);
    }
}
