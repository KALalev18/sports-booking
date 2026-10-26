package com.kalalev.booking.web;

import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.Facility;
import com.kalalev.booking.repository.CourtRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FacilityPageController {
    private final CourtRepository courtRepository;

    public FacilityPageController(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/facilities";
    }

    @GetMapping("/facilities")
    @Transactional(readOnly = true)
    public String facilities(Model model) {
        Map<Facility, List<Court>> courtsByFacility = courtRepository.findAllWithFacility().stream()
                .collect(Collectors.groupingBy(Court::getFacility, LinkedHashMap::new, Collectors.toList()));
        model.addAttribute("courtsByFacility", courtsByFacility);
        return "facilities";
    }
}
