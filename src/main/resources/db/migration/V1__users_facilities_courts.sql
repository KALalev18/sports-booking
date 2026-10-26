CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(10)  NOT NULL CHECK (role IN ('CUSTOMER', 'MANAGER', 'ADMIN')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- Emails are stored lower-case so the unique constraint is case-insensitive
    CHECK (email = lower(email))
);

CREATE TABLE facility (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    city       VARCHAR(60)  NOT NULL,
    address    VARCHAR(200) NOT NULL,
    -- Opening hours are local times in this zone; bookings are stored as UTC instants
    time_zone  VARCHAR(40)  NOT NULL DEFAULT 'Europe/Sofia',
    opens_at   TIME         NOT NULL,
    closes_at  TIME         NOT NULL,
    manager_id BIGINT REFERENCES app_user (id),
    CHECK (closes_at > opens_at)
);

CREATE INDEX facility_city_idx ON facility (lower(city));
CREATE INDEX facility_manager_idx ON facility (manager_id);

CREATE TABLE court (
    id             BIGSERIAL PRIMARY KEY,
    facility_id    BIGINT       NOT NULL REFERENCES facility (id),
    name           VARCHAR(60)  NOT NULL,
    sport          VARCHAR(20)  NOT NULL CHECK (sport IN ('FOOTBALL', 'TENNIS', 'BASKETBALL', 'VOLLEYBALL', 'PADEL')),
    price_per_hour NUMERIC(8, 2) NOT NULL CHECK (price_per_hour >= 0),
    UNIQUE (facility_id, name)
);

CREATE INDEX court_sport_idx ON court (sport, facility_id);
