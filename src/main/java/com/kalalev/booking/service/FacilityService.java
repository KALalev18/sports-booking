package com.kalalev.booking.service;

import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.Facility;
import com.kalalev.booking.domain.Sport;
import com.kalalev.booking.repository.CourtRepository;
import com.kalalev.booking.repository.FacilityRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacilityService {

    public record CourtView(long id, String name, Sport sport, BigDecimal pricePerHour) {}

    public record FacilityView(
            long id,
            String name,
            String city,
            String address,
            LocalTime opensAt,
            LocalTime closesAt,
            List<CourtView> courts) {}

    private final CourtRepository courtRepository;
    private final FacilityRepository facilityRepository;

    public FacilityService(CourtRepository courtRepository, FacilityRepository facilityRepository) {
        this.courtRepository = courtRepository;
        this.facilityRepository = facilityRepository;
    }

    @Transactional(readOnly = true)
    public List<FacilityView> listFacilities() {
        Map<Facility, List<Court>> courtsByFacility = courtRepository.findAllWithFacility().stream()
                .collect(Collectors.groupingBy(Court::getFacility, LinkedHashMap::new, Collectors.toList()));
        return courtsByFacility.entrySet().stream()
                .map(entry -> {
                    Facility f = entry.getKey();
                    List<CourtView> courts = entry.getValue().stream()
                            .map(c -> new CourtView(c.getId(), c.getName(), c.getSport(), c.getPricePerHour()))
                            .toList();
                    return new FacilityView(
                            f.getId(),
                            f.getName(),
                            f.getCity(),
                            f.getAddress(),
                            f.getOpensAt(),
                            f.getClosesAt(),
                            courts);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> cities() {
        return facilityRepository.findCities();
    }
}
