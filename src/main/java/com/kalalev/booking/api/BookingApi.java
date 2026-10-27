package com.kalalev.booking.api;

import com.kalalev.booking.service.BookingService;
import com.kalalev.booking.service.BookingView;
import com.kalalev.booking.service.RecurringBookingService;
import com.kalalev.booking.service.RecurringBookingService.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "The logged-in customer's bookings")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class BookingApi {

    /** @param start local time at the facility, on the hour, e.g. "18:00" */
    public record HoldRequest(
            @NotNull Long courtId,
            @NotNull LocalDate date,
            @NotNull LocalTime start,
            @Min(1) @Max(12) int hours) {}

    public record RecurringRequest(
            @NotNull Long courtId,
            @NotNull LocalDate firstDate,
            @NotNull LocalTime start,
            @Min(1) @Max(12) int hours,

            @Min(RecurringBookingService.MIN_WEEKS) @Max(RecurringBookingService.MAX_WEEKS)
            int weeks) {}

    private final BookingService bookingService;
    private final RecurringBookingService recurringBookingService;

    public BookingApi(BookingService bookingService, RecurringBookingService recurringBookingService) {
        this.bookingService = bookingService;
        this.recurringBookingService = recurringBookingService;
    }

    @PostMapping
    @Operation(summary = "Hold a slot for 10 minutes", description = "Returns 409 if the slot is taken.")
    public ResponseEntity<BookingView> hold(@Valid @RequestBody HoldRequest request, Authentication authentication) {
        BookingView booking = bookingService.hold(
                authentication.getName(), request.courtId(), request.date(), request.start(), request.hours());
        return ResponseEntity.created(URI.create("/api/v1/bookings/" + booking.id()))
                .body(booking);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a held slot before the hold runs out")
    public BookingView confirm(@PathVariable long id, Authentication authentication) {
        return bookingService.confirm(authentication.getName(), id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Release a hold, or cancel a booking up to 24 hours before it starts")
    public BookingView cancel(@PathVariable long id, Authentication authentication) {
        return bookingService.cancel(authentication, id);
    }

    @GetMapping
    @Operation(summary = "My bookings, newest first")
    public List<BookingView> mine(Authentication authentication) {
        return bookingService.bookingsFor(authentication.getName());
    }

    @GetMapping("/{id}")
    public BookingView get(@PathVariable long id, Authentication authentication) {
        return bookingService.getForCustomer(authentication.getName(), id);
    }

    @PostMapping("/recurring")
    @Operation(summary = "Book the same court every week; clashing weeks are reported and skipped")
    public ResponseEntity<Result> recurring(
            @Valid @RequestBody RecurringRequest request, Authentication authentication) {
        Result result = recurringBookingService.book(
                authentication.getName(),
                request.courtId(),
                request.firstDate(),
                request.start(),
                request.hours(),
                request.weeks());
        return ResponseEntity.status(201).body(result);
    }
}
