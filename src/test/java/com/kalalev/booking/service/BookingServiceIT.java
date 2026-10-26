package com.kalalev.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kalalev.booking.IntegrationTest;
import com.kalalev.booking.MutableClock;
import com.kalalev.booking.TestData;
import com.kalalev.booking.TestcontainersConfig;
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
        BookingView held = bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 2);

        assertThat(held.status()).isEqualTo(BookingStatus.HELD);
        assertThat(held.price()).isEqualByComparingTo("40.00");
        assertThat(held.holdExpiresAt()).isEqualTo(clock.instant().plus(Duration.ofMinutes(10)));

        BookingView confirmed = bookingService.confirm("ana@example.com", held.id());
        assertThat(confirmed.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(confirmed.holdExpiresAt()).isNull();
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
        BookingView next = bookingService.hold("boris@example.com", court, TOMORROW, LocalTime.of(19, 0), 1);

        assertThat(next.status()).isEqualTo(BookingStatus.HELD);
        assertThat(data.activeBookingsOn(court)).isEqualTo(2);
    }

    @Test
    void aRunOutHoldFreesTheSlotAndCanNoLongerBeConfirmed() {
        BookingView anasHold = bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 1);

        clock.advance(Duration.ofMinutes(10));
        BookingView borisHold = bookingService.hold("boris@example.com", court, TOMORROW, SIX_PM, 1);

        assertThat(borisHold.status()).isEqualTo(BookingStatus.HELD);
        assertReason(() -> bookingService.confirm("ana@example.com", anasHold.id()), Reason.HOLD_EXPIRED);
    }

    @Test
    void anExpiredHoldThatNobodyTookCannotBeConfirmed() {
        BookingView hold = bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 1);
        clock.advance(Duration.ofMinutes(11));

        assertReason(() -> bookingService.confirm("ana@example.com", hold.id()), Reason.HOLD_EXPIRED);
    }

    @Test
    void customersCannotConfirmSomeoneElsesHold() {
        BookingView hold = bookingService.hold("ana@example.com", court, TOMORROW, SIX_PM, 1);

        assertThatThrownBy(() -> bookingService.confirm("boris@example.com", hold.id()))
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
        BookingView early = confirmed("ana@example.com", IN_A_WEEK, SIX_PM);
        BookingView soon = confirmed("ana@example.com", TODAY, SIX_PM);

        assertThat(bookingService
                        .cancel(auth("ana@example.com", "CUSTOMER"), early.id())
                        .status())
                .isEqualTo(BookingStatus.CANCELLED);
        assertReason(
                () -> bookingService.cancel(auth("ana@example.com", "CUSTOMER"), soon.id()),
                Reason.CANCELLATION_WINDOW_CLOSED);
    }

    @Test
    void aCancelledSlotCanBeBookedAgain() {
        BookingView booking = confirmed("ana@example.com", IN_A_WEEK, SIX_PM);
        bookingService.cancel(auth("ana@example.com", "CUSTOMER"), booking.id());

        assertThat(bookingService
                        .hold("boris@example.com", court, IN_A_WEEK, SIX_PM, 1)
                        .status())
                .isEqualTo(BookingStatus.HELD);
    }

    @Test
    void theFacilityManagerCanCancelLateButOtherManagersCannotSeeTheBooking() {
        BookingView soon = confirmed("ana@example.com", TODAY, SIX_PM);

        assertThatThrownBy(() -> bookingService.cancel(auth("other.manager@example.com", "MANAGER"), soon.id()))
                .isInstanceOf(NotFoundException.class);
        assertThat(bookingService
                        .cancel(auth("manager@example.com", "MANAGER"), soon.id())
                        .status())
                .isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void maintenanceBlocksStopCustomersFromBooking() {
        actAs("manager@example.com", "MANAGER");
        BookingView block =
                bookingService.block("manager@example.com", court, TOMORROW, LocalTime.of(8, 0), 14, "Resurfacing");

        assertThat(block.price()).isEqualByComparingTo("0");
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

        List<BookingView> bookings = bookingService.bookingsFor("ana@example.com");
        assertThat(bookings).extracting(BookingView::start).isSortedAccordingTo((a, b) -> b.compareTo(a));
    }

    private BookingView confirmed(String email, LocalDate date, LocalTime start) {
        BookingView hold = bookingService.hold(email, court, date, start, 1);
        return bookingService.confirm(email, hold.id());
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
