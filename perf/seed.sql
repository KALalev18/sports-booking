-- Load-test dataset, added on top of the demo data:
-- 200 facilities in 10 cities, 5 courts each (1,000 courts), and 90 days of one-hour bookings
-- on roughly 35% of opening hours (about 440,000 bookings).
-- Run: docker compose exec -T postgres psql -U booking -d booking < perf/seed.sql

INSERT INTO app_user (email, password_hash, full_name, role)
SELECT 'perf' || g || '@example.com',
       (SELECT password_hash FROM app_user WHERE email = 'customer@example.com'),
       'Perf User ' || g,
       'CUSTOMER'
FROM generate_series(1, 200) g;

INSERT INTO facility (name, city, address, time_zone, opens_at, closes_at)
SELECT 'Perf Arena ' || g,
       (ARRAY['Sofia', 'Plovdiv', 'Varna', 'Burgas', 'Ruse', 'Stara Zagora', 'Pleven', 'Sliven', 'Dobrich', 'Shumen'])[1 + g % 10],
       'Street ' || g,
       'Europe/Sofia',
       '08:00',
       '22:00'
FROM generate_series(1, 200) g;

INSERT INTO court (facility_id, name, sport, price_per_hour)
SELECT f.id, 'Court ' || c, (ARRAY['FOOTBALL', 'TENNIS', 'BASKETBALL', 'VOLLEYBALL', 'PADEL'])[c], 20
FROM facility f
CROSS JOIN generate_series(1, 5) c
WHERE f.name LIKE 'Perf Arena %';

INSERT INTO booking (court_id, user_id, type, status, start_time, end_time, price, created_at)
SELECT c.id,
       (SELECT id FROM app_user WHERE email = 'perf200@example.com'),
       'CUSTOMER',
       'CONFIRMED',
       ((current_date + d) + make_time(h, 0, 0)) AT TIME ZONE 'Europe/Sofia',
       ((current_date + d) + make_time(h + 1, 0, 0)) AT TIME ZONE 'Europe/Sofia',
       20,
       now()
FROM court c
JOIN facility f ON f.id = c.facility_id
CROSS JOIN generate_series(1, 90) d
CROSS JOIN generate_series(8, 21) h
WHERE f.name LIKE 'Perf Arena %'
  AND random() < 0.35;

ANALYZE;

SELECT (SELECT count(*) FROM facility) AS facilities,
       (SELECT count(*) FROM court) AS courts,
       (SELECT count(*) FROM booking) AS bookings;
