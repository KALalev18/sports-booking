package com.kalalev.booking.web;

import com.kalalev.booking.service.BookingException;
import com.kalalev.booking.service.BookingService;
import com.kalalev.booking.service.BookingView;
import com.kalalev.booking.service.OccupancyService;
import com.kalalev.booking.service.OccupancyService.CourtDay;
import com.kalalev.booking.service.OccupancyService.FacilityDay;
import com.kalalev.booking.service.OccupancyService.FacilitySummary;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Manager pages. Access to each facility is checked by @PreAuthorize in the services. */
@Controller
@RequestMapping("/manage")
public class ManagePageController {

    /** One hour on one court in the day grid; {@code booking} is null when the hour is free. */
    public record Cell(LocalTime hour, BookingView booking, boolean first) {}

    public record Row(CourtDay court, List<Cell> cells) {
        static Row of(CourtDay court, List<LocalTime> hours) {
            List<Cell> cells = new ArrayList<>();
            for (LocalTime hour : hours) {
                BookingView covering = court.bookings().stream()
                        .filter(b -> !b.localStart().toLocalTime().isAfter(hour)
                                && b.localEnd().toLocalTime().isAfter(hour))
                        .findFirst()
                        .orElse(null);
                boolean first =
                        covering != null && covering.localStart().toLocalTime().equals(hour);
                cells.add(new Cell(hour, covering, first));
            }
            return new Row(court, cells);
        }
    }

    private final OccupancyService occupancyService;
    private final BookingService bookingService;
    private final Clock clock;

    public ManagePageController(OccupancyService occupancyService, BookingService bookingService, Clock clock) {
        this.occupancyService = occupancyService;
        this.bookingService = bookingService;
        this.clock = clock;
    }

    @GetMapping
    public String facilities(Authentication authentication, Model model) {
        List<FacilitySummary> facilities = occupancyService.facilitiesFor(authentication);
        if (facilities.size() == 1) {
            return "redirect:/manage/facilities/" + facilities.get(0).id();
        }
        model.addAttribute("facilities", facilities);
        return "manage";
    }

    @GetMapping("/facilities/{id}")
    public String day(
            @PathVariable long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        LocalDate day = date != null ? date : LocalDate.now(clock.withZone(SearchController.DEFAULT_ZONE));
        FacilityDay facilityDay = occupancyService.day(id, day);
        List<LocalTime> hours = new ArrayList<>();
        LocalTime hour = facilityDay.opensAt();
        while (hour.plusHours(1).isAfter(hour) && !hour.plusHours(1).isAfter(facilityDay.closesAt())) {
            hours.add(hour);
            hour = hour.plusHours(1);
        }
        model.addAttribute("day", facilityDay);
        model.addAttribute("hours", hours);
        model.addAttribute(
                "rows", facilityDay.courts().stream().map(c -> Row.of(c, hours)).toList());
        model.addAttribute("previous", day.minusDays(1));
        model.addAttribute("next", day.plusDays(1));
        return "manage-day";
    }

    @PostMapping("/facilities/{id}/blocks")
    public String block(
            @PathVariable long id,
            @RequestParam long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam int hours,
            @RequestParam(required = false) String note,
            Authentication authentication,
            RedirectAttributes redirect) {
        try {
            bookingService.block(authentication.getName(), courtId, date, start, hours, note);
            redirect.addFlashAttribute("success", "Court blocked.");
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manage/facilities/" + id + "?date=" + date;
    }

    @PostMapping("/facilities/{id}/bookings/{bookingId}/cancel")
    public String cancel(
            @PathVariable long id,
            @PathVariable long bookingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication,
            RedirectAttributes redirect) {
        try {
            bookingService.cancel(authentication, bookingId);
            redirect.addFlashAttribute("success", "Booking cancelled. The customer gets an email.");
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manage/facilities/" + id + "?date=" + date;
    }
}
