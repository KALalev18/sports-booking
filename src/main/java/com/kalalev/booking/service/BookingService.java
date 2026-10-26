package com.kalalev.booking.service;

import static com.kalalev.booking.service.BookingException.Reason.CANCELLATION_WINDOW_CLOSED;
import static com.kalalev.booking.service.BookingException.Reason.HOLD_EXPIRED;
import static com.kalalev.booking.service.BookingException.Reason.INVALID_STATE;
import static com.kalalev.booking.service.BookingException.Reason.SLOT_UNAVAILABLE;
import static com.kalalev.booking.service.BookingException.Reason.TOO_MANY_HOLDS;

import com.kalalev.booking.domain.AppUser;
import com.kalalev.booking.domain.Booking;
import com.kalalev.booking.domain.BookingStatus;
import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.TimeSlot;
import com.kalalev.booking.repository.BookingRepository;
import com.kalalev.booking.repository.CourtRepository;
import com.kalalev.booking.security.FacilityAccess;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Holds, confirms and cancels bookings.
 *
 * <p>Two layers stop double bookings:
 *
 * <ol>
 *   <li>Each transaction that adds a booking first locks the court row ({@link CourtRepository#findForUpdate}), so
 *       requests for the same court are handled one at a time and cannot deadlock each other.
 *   <li>The {@code booking_no_overlap} exclusion constraint in PostgreSQL rejects any overlapping active booking. This
 *       is the guarantee: it holds even for code that forgets the lock, or for SQL run by hand.
 * </ol>
 *
 * <p>State changes (confirm, cancel, expire) use optimistic locking through the {@code version} column.
 */
@Service
public class BookingService {
    /** SQLSTATE PostgreSQL uses when an exclusion constraint is violated. */
    private static final String EXCLUSION_VIOLATION = "23P01";

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserService userService;
    private final BookingPolicy policy;
    private final FacilityAccess facilityAccess;

    public BookingService(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            UserService userService,
            BookingPolicy policy,
            FacilityAccess facilityAccess) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.userService = userService;
        this.policy = policy;
        this.facilityAccess = facilityAccess;
    }

    /** Holds a slot for the customer; it must be confirmed before the hold runs out. */
    @Transactional
    public Booking hold(String email, long courtId, LocalDate date, LocalTime start, int hours) {
        AppUser customer = userService.getByEmail(email);
        Court court = lockCourt(courtId);
        TimeSlot slot = policy.customerSlot(court, date, start, hours);
        Instant now = policy.now();

        if (bookingRepository.countActiveHolds(customer, now) >= policy.maxActiveHolds()) {
            throw new BookingException(
                    TOO_MANY_HOLDS, "You can hold at most " + policy.maxActiveHolds() + " slots at a time");
        }
        bookingRepository.expireRunOutHolds(courtId, slot.start(), slot.end(), now);
        return insert(Booking.hold(court, customer, slot, policy.price(court, slot), policy.holdExpiry(), now));
    }

    @Transactional
    public Booking confirm(String email, long bookingId) {
        Booking booking = findBooking(bookingId);
        if (!booking.getUser().getEmail().equals(email)) {
            throw new NotFoundException("No booking " + bookingId);
        }
        if (booking.getStatus() != BookingStatus.HELD) {
            throw new BookingException(
                    INVALID_STATE,
                    "This booking is " + booking.getStatus().name().toLowerCase());
        }
        if (booking.isHoldExpired(policy.now())) {
            throw new BookingException(HOLD_EXPIRED, "Your hold has expired. The slot may still be free.");
        }
        booking.confirm();
        flushOrConflict("Your hold expired while you were confirming it");
        return booking;
    }

    /**
     * Customers can cancel their own holds at any time and confirmed bookings until the free cancellation window
     * closes. Managers of the facility and admins can cancel any active booking there, for example for bad weather.
     */
    @Transactional
    public Booking cancel(Authentication authentication, long bookingId) {
        Booking booking = findBooking(bookingId);
        boolean owner = booking.getUser().getEmail().equals(authentication.getName());
        boolean staff = facilityAccess.canManage(booking.getCourt().getFacility(), authentication);
        if (!owner && !staff) {
            throw new NotFoundException("No booking " + bookingId);
        }
        if (!booking.isActive()) {
            throw new BookingException(
                    INVALID_STATE,
                    "This booking is already " + booking.getStatus().name().toLowerCase());
        }
        if (!staff && !policy.customerCanCancel(booking)) {
            throw new BookingException(
                    CANCELLATION_WINDOW_CLOSED,
                    "Bookings can only be cancelled up to " + policy.freeCancellationHours()
                            + " hours before they start");
        }
        booking.cancel();
        flushOrConflict("The booking changed while you were cancelling it. Please reload.");
        return booking;
    }

    /** Closes a court for maintenance. Fails if customers have booked any part of the time; cancel those first. */
    @PreAuthorize("@facilityAccess.canManageCourt(#courtId, authentication)")
    @Transactional
    public Booking block(String managerEmail, long courtId, LocalDate date, LocalTime start, int hours, String note) {
        AppUser manager = userService.getByEmail(managerEmail);
        Court court = lockCourt(courtId);
        TimeSlot slot = policy.slotFor(court, date, start, hours, 24);
        Instant now = policy.now();

        bookingRepository.expireRunOutHolds(courtId, slot.start(), slot.end(), now);
        List<Booking> overlapping = bookingRepository.findOccupying(List.of(courtId), slot.start(), slot.end(), now);
        if (!overlapping.isEmpty()) {
            throw new BookingException(
                    SLOT_UNAVAILABLE, overlapping.size() + " booking(s) overlap this time. Cancel them first.");
        }
        return insert(Booking.maintenance(court, manager, slot, note, now));
    }

    @Transactional(readOnly = true)
    public List<Booking> bookingsFor(String email) {
        return bookingRepository.findForUser(userService.getByEmail(email));
    }

    @Transactional(readOnly = true)
    public Booking getForCustomer(String email, long bookingId) {
        Booking booking = findBooking(bookingId);
        if (!booking.getUser().getEmail().equals(email)) {
            throw new NotFoundException("No booking " + bookingId);
        }
        return booking;
    }

    private Booking insert(Booking booking) {
        try {
            return bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            if (isOverlap(e)) {
                throw new BookingException(SLOT_UNAVAILABLE, "Someone else has just booked this time");
            }
            throw e;
        }
    }

    private void flushOrConflict(String message) {
        try {
            bookingRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BookingException(HOLD_EXPIRED, message);
        }
    }

    private static boolean isOverlap(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && EXCLUSION_VIOLATION.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }

    private Court lockCourt(long courtId) {
        return courtRepository.findForUpdate(courtId).orElseThrow(() -> new NotFoundException("No court " + courtId));
    }

    private Booking findBooking(long bookingId) {
        return bookingRepository
                .findWithCourt(bookingId)
                .orElseThrow(() -> new NotFoundException("No booking " + bookingId));
    }
}
