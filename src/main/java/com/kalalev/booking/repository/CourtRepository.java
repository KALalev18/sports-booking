package com.kalalev.booking.repository;

import com.kalalev.booking.domain.Court;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourtRepository extends JpaRepository<Court, Long> {
    @Query("select c from Court c join fetch c.facility f order by f.city, f.name, c.name")
    List<Court> findAllWithFacility();

    /**
     * Loads the court with a row lock (SELECT ... FOR UPDATE) held until the transaction ends. Every transaction
     * that inserts a booking takes this lock first, so writes to one court happen one after another. Without it,
     * two inserts for the same slot can each wait for the other's uncommitted row while checking the exclusion
     * constraint, and PostgreSQL aborts one of them as a deadlock (found by ConcurrentBookingIT). Different courts
     * do not block each other.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Court c where c.id = :id")
    Optional<Court> findForUpdate(@Param("id") long id);
}
