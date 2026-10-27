package com.kalalev.booking.web;

import com.kalalev.booking.service.FacilityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FacilityPageController {
    private final FacilityService facilityService;

    public FacilityPageController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping("/facilities")
    public String facilities(Model model) {
        model.addAttribute("facilities", facilityService.listFacilities());
        return "facilities";
    }
}
