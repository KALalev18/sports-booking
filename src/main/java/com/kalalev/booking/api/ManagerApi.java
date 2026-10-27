package com.kalalev.booking.api;

import com.kalalev.booking.service.BookingService;
import com.kalalev.booking.service.BookingView;
import com.kalalev.booking.service.OccupancyService;
import com.kalalev.booking.service.OccupancyService.FacilityDay;
import com.kalalev.booking.service.OccupancyService.FacilitySummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** For facility managers and admins. Which facility a manager may see is checked in the services. */
@RestController
@RequestMapping("/api/v1/manage")
@Tag(name = "Facility management", description = "MANAGER of the facility, or ADMIN")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class ManagerApi {

    public record BlockRequest(
            @NotNull LocalDate date,
            @NotNull LocalTime start,
            @Min(1) @Max(24) int hours,
            @Size(max = 200) String note) {}

    private final OccupancyService occupancyService;
    private final BookingService bookingService;

    public ManagerApi(OccupancyService occupancyService, BookingService bookingService) {
        this.occupancyService = occupancyService;
        this.bookingService = bookingService;
    }

    @GetMapping("/facilities")
    @Operation(summary = "Facilities I manage (all of them for admins)")
    public List<FacilitySummary> facilities(Authentication authentication) {
        return occupancyService.facilitiesFor(authentication);
    }

    @GetMapping("/facilities/{facilityId}/occupancy")
    @Operation(summary = "Every court's bookings for a day and the share of hours booked")
    public FacilityDay occupancy(
            @PathVariable long facilityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return occupancyService.day(facilityId, date);
    }

    @PostMapping("/courts/{courtId}/blocks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Close a court for maintenance; fails with 409 if customers have booked that time")
    public BookingView block(
            @PathVariable long courtId, @Valid @RequestBody BlockRequest request, Authentication authentication) {
        return bookingService.block(
                authentication.getName(), courtId, request.date(), request.start(), request.hours(), request.note());
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    @Operation(summary = "Cancel any booking at my facility, at any time")
    public BookingView cancel(@PathVariable long bookingId, Authentication authentication) {
        return bookingService.cancel(authentication, bookingId);
    }
}
