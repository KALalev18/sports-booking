package com.kalalev.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kalalev.booking.IntegrationTest;
import com.kalalev.booking.MutableClock;
import com.kalalev.booking.TestData;
import com.kalalev.booking.TestcontainersConfig;
import com.kalalev.booking.domain.Booking;
import com.kalalev.booking.domain.BookingStatus;
import com.kalalev.booking.service.BookingException.Reason;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@IntegrationTest
class BookingServiceIT {
    // The test clock starts on 1 June 2026 at 09:00 in the facility's time zone
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);
    private static final LocalDate TOMORROW = LocalDate.of(2026, 6, 2);
    private static final LocalDate IN_A_WEEK = LocalDate.of(2026, 6, 8);
    private static final LocalTime SIX_PM = LocalTime.of(18, 0);

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
        long manager = data.user("manager@example.com", "MANAGER");
        data.user("other.manager@example.com", "MANAGER");
        long facility = data.facility("Sea Garden Courts", "Varna", manager);
        court = data.court(facility, "Court 1", "TENNIS", "20.00");
        data.customer("ana@example.com");
        data.customer("boris@example.com");
        SecurityContextHolder.clearContext();
    }

    @Test
    void holdsAndConfirmsASlot() {
        Booking held = bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 2);

        assertThat(held.getStatus()).isEqualTo(BookingStatus.HELD);
        assertThat(held.getPrice()).isEqualByComparingTo("40.00");
        assertThat(held.getHoldExpiresAt()).isEqualTo(clock.instant().plus(Duration.ofMinutes(10)));

        Booking confirmed = bookingService.confirm("ana@example.com", held.getId());
        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(confirmed.getHoldExpiresAt()).isNull();
    }

    @Test
    void aHeldSlotCannotBeTakenByAnotherCustomer() {
        bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 2);

        assertReason(
                () -> bookingService.hold("boris@example.com", court, TOMORROW, LocalTime.of(19, 0), 1),
                Reason.SLOT_UNAVAILABLE);
    }

    @Test
    void backToBackSlotsDoNotConflict() {
        bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 1);
        Booking next = bookingService.hold("boris@example.com", court, TOMORROW, LocalTime.of(19, 0), 1);

        assertThat(next.getStatus()).isEqualTo(BookingStatus.HELD);
        assertThat(data.activeBookingsOn(court)).isEqualTo(2);
    }

    @Test
    void aRunOutHoldFreesTheSlotAndCanNoLongerBeConfirmed() {
        Booking anasHold = bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 1);

        clock.advance(Duration.ofMinutes(10));
        Booking borisHold = bookingService.hold("boris@example.com", court, TOMORROW, SIX_PM, 1);

        assertThat(borisHold.getStatus()).isEqualTo(BookingStatus.HELD);
        assertReason(() -> bookingService.confirm("ana@example.com", anasHold.getId()), Reason.HOLD_EXPIRED);
    }

    @Test
    void anExpiredHoldThatNobodyTookCannotBeConfirmed() {
        Booking hold = bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 1);
        clock.advance(Duration.ofMinutes(11));

        assertReason(() -> bookingService.confirm("ana@example.com", hold.getId()), Reason.HOLD_EXPIRED);
    }

    @Test
    void customersCannotConfirmSomeoneElsesHold() {
        Booking hold = bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 1);

        assertThatThrownBy(() -> bookingService.confirm("boris@example.com", hold.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void customersCanHoldAtMostThreeSlotsAtOnce() {
        for (int hour = 10; hour < 13; hour++) {
            bookingService.hold("ana@example.com", court, IN_A_WEEK, LocalTime.of(hour, 0), 1);
        }

        assertReason(
                () -> bookingService.hold("ana@example.com", court, IN_A_WEEK, LocalTime.of(14, 0), 1),
                Reason.TOO_MANY_HOLDS);
    }

    @Test
    void customersCanCancelUntil24HoursBeforeTheStart() {
        Booking early = confirmed("ana@example.com", IN_A_WEEK, SIX_PM);
        Booking soon = confirmed("ana@example.com", TODAY, SIX_PM);

        assertThat(bookingService
                        .cancel(auth("ana@example.com", "CUSTOMER"), early.getId())
                        .getStatus())
                .isEqualTo(BookingStatus.CANCELLED);
        assertReason(
                () -> bookingService.cancel(auth("ana@example.com", "CUSTOMER"), soon.getId()),
                Reason.CANCELLATION_WINDOW_CLOSED);
    }

    @Test
    void aCancelledSlotCanBeBookedAgain() {
        Booking booking = confirmed("ana@example.com", IN_A_WEEK, SIX_PM);
        bookingService.cancel(auth("ana@example.com", "CUSTOMER"), booking.getId());

        assertThat(bookingService
                        .hold("boris@example.com", court, IN_A_WEEK, SIX_PM, 1)
                        .getStatus())
                .isEqualTo(BookingStatus.HELD);
    }

    @Test
    void theFacilityManagerCanCancelLateButOtherManagersCannotSeeTheBooking() {
        Booking soon = confirmed("ana@example.com", TODAY, SIX_PM);

        assertThatThrownBy(() -> bookingService.cancel(auth("other.manager@example.com", "MANAGER"), soon.getId()))
                .isInstanceOf(NotFoundException.class);
        assertThat(bookingService
                        .cancel(auth("manager@example.com", "MANAGER"), soon.getId())
                        .getStatus())
                .isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void maintenanceBlocksStopCustomersFromBooking() {
        actAs("manager@example.com", "MANAGER");
        Booking block =
                bookingService.block("manager@example.com", court, TOMORROW, LocalTime.of(8, 0), 14, "Resurfacing");

        assertThat(block.getPrice()).isEqualByComparingTo("0");
        assertReason(() -> bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 1), Reason.SLOT_UNAVAILABLE);
    }

    @Test
    void aBlockOverACustomerBookingIsRefused() {
        confirmed("ana@example.com", TOMORROW, SIX_PM);
        actAs("manager@example.com", "MANAGER");

        assertReason(
                () -> bookingService.block("manager@example.com", court, TOMORROW, LocalTime.of(17, 0), 3, "Repairs"),
                Reason.SLOT_UNAVAILABLE);
    }

    @Test
    void onlyTheFacilitysManagerCanBlockItsCourts() {
        actAs("other.manager@example.com", "MANAGER");

        assertThatThrownBy(
                        () -> bookingService.block("other.manager@example.com", court, TOMORROW, SIX_PM, 1, "Not mine"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void listsACustomersBookingsNewestFirst() {
        confirmed("ana@example.com", TOMORROW, SIX_PM);
        confirmed("ana@example.com", IN_A_WEEK, SIX_PM);

        List<Booking> bookings = bookingService.bookingsFor("ana@example.com");
        assertThat(bookings).extracting(Booking::getStartTime).isSortedAccordingTo((a, b) -> b.compareTo(a));
    }

    private Booking confirmed(String email, LocalDate date, LocalTime start) {
        Booking hold = bookingService.hold(email, court, date, start, 1);
        return bookingService.confirm(email, hold.getId());
    }

    private static Authentication auth(String email, String role) {
        return new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private static void actAs(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(auth(email, role));
    }

    private static void assertReason(ThrowingCallable call, Reason reason) {
        assertThatThrownBy(call)
                .isInstanceOf(BookingException.class)
                .extracting(e -> ((BookingException) e).getReason())
                .isEqualTo(reason);
    }
}
