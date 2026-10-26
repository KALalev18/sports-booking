package com.kalalev.booking.api;

import com.kalalev.booking.domain.Sport;
import com.kalalev.booking.service.AvailabilityService;
import com.kalalev.booking.service.AvailabilityService.CourtAvailability;
import com.kalalev.booking.service.FacilityService;
import com.kalalev.booking.service.FacilityService.FacilityView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Facilities and availability", description = "Public, no token needed")
public class FacilityApi {
    private final FacilityService facilityService;
    private final AvailabilityService availabilityService;

    public FacilityApi(FacilityService facilityService, AvailabilityService availabilityService) {
        this.facilityService = facilityService;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/facilities")
    @Operation(summary = "List facilities with their courts and prices")
    public List<FacilityView> facilities() {
        return facilityService.listFacilities();
    }

    @GetMapping("/availability")
    @Operation(summary = "Free one-hour slots per court for a date, optionally filtered by city and sport")
    public List<CourtAvailability> availability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Sport sport) {
        return availabilityService.search(city, sport, date);
    }
}
