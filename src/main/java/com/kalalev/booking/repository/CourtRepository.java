package com.kalalev.booking.repository;

import com.kalalev.booking.domain.Court;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourtRepository extends JpaRepository<Court, Long> {
    @Query("select c from Court c join fetch c.facility f order by f.city, f.name, c.name")
    List<Court> findAllWithFacility();
}
