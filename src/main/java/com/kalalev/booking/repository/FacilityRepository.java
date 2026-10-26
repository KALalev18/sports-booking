package com.kalalev.booking.repository;

import com.kalalev.booking.domain.Facility;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    @Query("select distinct f.city from Facility f order by f.city")
    List<String> findCities();

    List<Facility> findAllByOrderByCityAscNameAsc();
}
