package com.kalalev.booking.repository;

import com.kalalev.booking.domain.AppUser;
import com.kalalev.booking.domain.Booking;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Bookings that occupy any of these courts during [from, to). Holds whose time ran out are treated as free even
     * if the expiry job has not marked them yet. The tstzrange overlap uses the GiST index behind the
     * booking_no_overlap constraint.
     */
    @Query(nativeQuery = true, value = """
            SELECT * FROM booking
            WHERE court_id IN (:courtIds)
              AND tstzrange(start_time, end_time, '[)') && tstzrange(:from, :to, '[)')
              AND (status = 'CONFIRMED' OR (status = 'HELD' AND hold_expires_at > :now))
            ORDER BY start_time
            """)
    List<Booking> findOccupying(
            @Param("courtIds") Collection<Long> courtIds,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("now") Instant now);

    /**
     * Marks run-out holds on one court and time range as EXPIRED, so a new booking for that time is not blocked by
     * the constraint while it waits for the expiry job. Bumps the version so a concurrent confirm of the same hold
     * fails its optimistic lock check.
     */
    @Modifying(flushAutomatically = true)
    @Query(nativeQuery = true, value = """
            UPDATE booking SET status = 'EXPIRED', hold_expires_at = NULL, version = version + 1
            WHERE court_id = :courtId
              AND status = 'HELD' AND hold_expires_at <= :now
              AND tstzrange(start_time, end_time, '[)') && tstzrange(:from, :to, '[)')
            """)
    int expireRunOutHolds(
            @Param("courtId") long courtId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("now") Instant now);

    /** Releases every hold whose time has run out. Used by the scheduled expiry job. */
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE booking SET status = 'EXPIRED', hold_expires_at = NULL, version = version + 1
            WHERE status = 'HELD' AND hold_expires_at <= :now
            """)
    int expireAllRunOutHolds(@Param("now") Instant now);

    @Query("""
            select count(b) from Booking b
            where b.user = :user and b.status = com.kalalev.booking.domain.BookingStatus.HELD
              and b.holdExpiresAt > :now
            """)
    long countActiveHolds(@Param("user") AppUser user, @Param("now") Instant now);

    @Query("""
            select b from Booking b join fetch b.court c join fetch c.facility
            where b.user = :user order by b.startTime desc
            """)
    List<Booking> findForUser(@Param("user") AppUser user);

    @Query(
            "select b from Booking b join fetch b.court c join fetch c.facility f left join fetch f.manager where b.id = :id")
    Optional<Booking> findWithCourt(@Param("id") long id);
}
