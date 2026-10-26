-- btree_gist lets one GiST index combine "same court" (=) with "time ranges overlap" (&&)
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE booking (
    id              BIGSERIAL PRIMARY KEY,
    court_id        BIGINT        NOT NULL REFERENCES court (id),
    user_id         BIGINT        NOT NULL REFERENCES app_user (id),
    type            VARCHAR(12)   NOT NULL CHECK (type IN ('CUSTOMER', 'MAINTENANCE')),
    status          VARCHAR(10)   NOT NULL CHECK (status IN ('HELD', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    start_time      TIMESTAMPTZ   NOT NULL,
    end_time        TIMESTAMPTZ   NOT NULL,
    price           NUMERIC(8, 2) NOT NULL CHECK (price >= 0),
    hold_expires_at TIMESTAMPTZ,
    note            VARCHAR(200),
    created_at      TIMESTAMPTZ   NOT NULL,
    -- Optimistic locking: every update must match the version it read
    version         BIGINT        NOT NULL DEFAULT 0,
    CHECK (end_time > start_time),
    CHECK (status <> 'HELD' OR hold_expires_at IS NOT NULL),

    -- The rule the whole system relies on: two active bookings on the same court can never overlap.
    -- PostgreSQL checks this on every insert and update, so it holds even when many requests arrive
    -- at once. '[)' makes ranges half-open, so 10:00-11:00 and 11:00-12:00 do not conflict.
    -- Cancelled and expired rows are ignored, which frees their slot.
    CONSTRAINT booking_no_overlap EXCLUDE USING gist (
        court_id WITH =,
        tstzrange(start_time, end_time, '[)') WITH &&
    ) WHERE (status IN ('HELD', 'CONFIRMED'))
);

-- "My bookings", newest first
CREATE INDEX booking_user_idx ON booking (user_id, start_time DESC);
-- The expiry job only scans holds
CREATE INDEX booking_held_expiry_idx ON booking (hold_expires_at) WHERE status = 'HELD';
