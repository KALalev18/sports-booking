package com.kalalev.booking.web;

import com.kalalev.booking.config.BookingProperties;
import com.kalalev.booking.service.BookingException;
import com.kalalev.booking.service.BookingService;
import com.kalalev.booking.service.BookingView;
import com.kalalev.booking.service.FacilityService;
import com.kalalev.booking.service.RecurringBookingService;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
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

@Controller
@RequestMapping("/bookings")
public class BookingPageController {
    private final BookingService bookingService;
    private final RecurringBookingService recurringBookingService;
    private final FacilityService facilityService;
    private final Clock clock;
    private final Duration cancellationWindow;

    public BookingPageController(
            BookingService bookingService,
            RecurringBookingService recurringBookingService,
            FacilityService facilityService,
            Clock clock,
            BookingProperties properties) {
        this.bookingService = bookingService;
        this.recurringBookingService = recurringBookingService;
        this.facilityService = facilityService;
        this.clock = clock;
        this.cancellationWindow = properties.freeCancellationWindow();
    }

    /** A slot button on the search page; holds the slot and opens checkout. */
    @PostMapping
    public String hold(
            @RequestParam long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam(defaultValue = "1") int hours,
            @RequestParam(defaultValue = "/") String back,
            Authentication authentication,
            RedirectAttributes redirect) {
        try {
            BookingView booking = bookingService.hold(authentication.getName(), courtId, date, start, hours);
            return "redirect:/bookings/" + booking.id();
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:" + (back.startsWith("/") && !back.startsWith("//") ? back : "/");
        }
    }

    @GetMapping("/{id}")
    public String checkout(@PathVariable long id, Authentication authentication, Model model) {
        model.addAttribute("booking", bookingService.getForCustomer(authentication.getName(), id));
        model.addAttribute("now", clock.instant());
        return "checkout";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable long id, Authentication authentication, RedirectAttributes redirect) {
        try {
            bookingService.confirm(authentication.getName(), id);
            redirect.addFlashAttribute("success", "Booked! A confirmation email is on its way.");
            return "redirect:/bookings";
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings/" + id;
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable long id, Authentication authentication, RedirectAttributes redirect) {
        try {
            BookingView booking = bookingService.cancel(authentication, id);
            redirect.addFlashAttribute("success", "Cancelled your booking at " + booking.facilityName() + ".");
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings";
    }

    @GetMapping
    public String mine(Authentication authentication, Model model) {
        List<BookingView> bookings = bookingService.bookingsFor(authentication.getName());
        var now = clock.instant();
        model.addAttribute(
                "upcoming",
                bookings.stream()
                        .filter(b -> b.active() && b.end().isAfter(now))
                        .sorted((a, b) -> a.start().compareTo(b.start()))
                        .toList());
        model.addAttribute(
                "past",
                bookings.stream()
                        .filter(b -> !b.active() || !b.end().isAfter(now))
                        .toList());
        model.addAttribute("now", now);
        // Confirmed bookings that start after this instant can still be cancelled for free
        model.addAttribute("cancelCutoff", now.plus(cancellationWindow));
        return "bookings";
    }

    @GetMapping("/recurring")
    public String recurringForm(Model model) {
        model.addAttribute("facilities", facilityService.listFacilities());
        model.addAttribute("minWeeks", RecurringBookingService.MIN_WEEKS);
        model.addAttribute("maxWeeks", RecurringBookingService.MAX_WEEKS);
        model.addAttribute(
                "firstDate",
                LocalDate.now(clock.withZone(SearchController.DEFAULT_ZONE)).plusDays(1));
        return "recurring";
    }

    @PostMapping("/recurring")
    public String recurring(
            @RequestParam long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate firstDate,
            @RequestParam LocalTime start,
            @RequestParam int hours,
            @RequestParam int weeks,
            Authentication authentication,
            Model model,
            RedirectAttributes redirect) {
        try {
            model.addAttribute(
                    "result",
                    recurringBookingService.book(authentication.getName(), courtId, firstDate, start, hours, weeks));
            return "recurring-result";
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings/recurring";
        }
    }
}
