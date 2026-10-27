# Measured results

Measured on 27 October 2026 on a laptop: AMD Ryzen 7 4800H (8 cores / 16 threads), 15 GB RAM, Windows 11,
Docker Desktop 29.8 with 8 GB for containers. App, PostgreSQL 17.11 and k6 all ran on that machine in Docker, so
these numbers show the design holds up under load, not what a production server would do.

## Dataset

[`seed.sql`](seed.sql) on top of the demo data: **204 facilities, 1,011 courts, 441,277 bookings** (90 days,
about 35% of opening hours booked).

```bash
docker compose --profile app up --build -d
docker compose exec -T postgres psql -U booking -d booking < perf/seed.sql
```

## Availability query plan

The search query for one city (100 courts) on one day, run with `EXPLAIN (ANALYZE, BUFFERS)`:

```
Index Scan using booking_no_overlap on booking  (actual time=0.082..0.090 rows=5 loops=100)
  Index Cond: ((court_id = c.id) AND (tstzrange(start_time, end_time, '[)') && tstzrange(...)))
Execution Time: 10.840 ms
```

The GiST index that enforces the no-overlap rule is also the index the search uses, so no second index is needed.

## Load tests (k6 1.8.2)

Run from the k6 Docker image on the Compose network:

```bash
docker run --rm --network <project>_default -e BASE=http://app:8080 -v "$PWD/perf:/scripts" \
  grafana/k6:1.8.2 run /scripts/<script>.js
```

| Script | What it does | Result |
| --- | --- | --- |
| [`availability.js`](availability.js) | 20 virtual users search a random city and day for 60 s | **593 requests/s**, p50 31 ms, **p95 46 ms**, p99 58 ms, 0 errors |
| [`booking.js`](booking.js) | 50 customers hold random slots for 60 s, releasing each successful hold | **568 hold attempts/s** (368/s created, 199/s refused with 409 because the slot was taken), hold p95 **78 ms**, all checks passed |
| [`same-slot.js`](same-slot.js) | 100 customers try to hold the same free slot at the same moment | **1 created, 99 refused (409)** |

k6 reports the 409 answers in `booking.js` as failed requests (21%); they are the expected response for a taken slot.

## Tests and coverage

`./mvnw verify`: 23 unit tests and 47 integration tests against PostgreSQL in Testcontainers, all passing.

JaCoCo, merged over both: **78.9% of lines, 64.2% of branches**. By package (lines): service 89.5%, security 89.9%,
domain 93.0%, web 72.6%, api 68.9%. The demo data seeder (config) is not run by tests.
