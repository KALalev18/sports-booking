package com.kalalev.booking.web;

import com.kalalev.booking.domain.Sport;
import com.kalalev.booking.service.AvailabilityService;
import com.kalalev.booking.service.FacilityService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {
    /** All demo facilities are in Bulgaria; "today" in the search form means today there. */
    static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Sofia");

    private final AvailabilityService availabilityService;
    private final FacilityService facilityService;
    private final Clock clock;

    public SearchController(AvailabilityService availabilityService, FacilityService facilityService, Clock clock) {
        this.availabilityService = availabilityService;
        this.facilityService = facilityService;
        this.clock = clock;
    }

    @GetMapping("/")
    public String search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Sport sport,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        LocalDate today = LocalDate.now(clock.withZone(DEFAULT_ZONE));
        LocalDate day = date == null || date.isBefore(today) ? today : date;
        model.addAttribute("cities", facilityService.cities());
        model.addAttribute("sports", Sport.values());
        model.addAttribute("city", city);
        model.addAttribute("sport", sport);
        model.addAttribute("date", day);
        model.addAttribute("today", today);
        model.addAttribute("results", availabilityService.search(city, sport, day));
        return "search";
    }
}
