package com.kalalev.booking;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Creates rows with plain SQL so tests do not depend on the code they are testing. Ids come from RETURNING.
 * Every user's password is "password".
 */
public class TestData {
    public static final String PASSWORD = "password";
    private static final String PASSWORD_HASH = new BCryptPasswordEncoder().encode(PASSWORD);

    private final JdbcTemplate jdbc;

    public TestData(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void reset() {
        jdbc.execute("TRUNCATE booking, court, facility, app_user RESTART IDENTITY CASCADE");
    }

    public long user(String email, String role) {
        return jdbc.queryForObject(
                "INSERT INTO app_user (email, password_hash, full_name, role) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class,
                email,
                PASSWORD_HASH,
                email.substring(0, email.indexOf('@')),
                role);
    }

    public long customer(String email) {
        return user(email, "CUSTOMER");
    }

    /** A facility in Sofia open 08:00–22:00, optionally run by a manager. */
    public long facility(String name, String city, Long managerId) {
        return jdbc.queryForObject("""
                INSERT INTO facility (name, city, address, time_zone, opens_at, closes_at, manager_id)
                VALUES (?, ?, 'Test street 1', 'Europe/Sofia', '08:00', '22:00', ?) RETURNING id
                """, Long.class, name, city, managerId);
    }

    public long court(long facilityId, String name, String sport, String pricePerHour) {
        return jdbc.queryForObject(
                "INSERT INTO court (facility_id, name, sport, price_per_hour) VALUES (?, ?, ?, ?::numeric) RETURNING id",
                Long.class,
                facilityId,
                name,
                sport,
                pricePerHour);
    }

    public int activeBookingsOn(long courtId) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM booking WHERE court_id = ? AND status IN ('HELD', 'CONFIRMED')",
                Integer.class,
                courtId);
    }

    public JdbcTemplate jdbc() {
        return jdbc;
    }
}
