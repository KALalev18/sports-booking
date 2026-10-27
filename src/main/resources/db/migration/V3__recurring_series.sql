-- A weekly booking such as "every Tuesday 18:00-20:00 for 10 weeks". Each week is its own row in booking,
-- so the overlap constraint applies to every occurrence.
CREATE TABLE recurring_series (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES app_user (id),
    court_id    BIGINT      NOT NULL REFERENCES court (id),
    first_date  DATE        NOT NULL,
    start_local TIME        NOT NULL,
    hours       SMALLINT    NOT NULL CHECK (hours > 0),
    weeks       SMALLINT    NOT NULL CHECK (weeks BETWEEN 2 AND 12),
    created_at  TIMESTAMPTZ NOT NULL
);

ALTER TABLE booking ADD COLUMN series_id BIGINT REFERENCES recurring_series (id);

CREATE INDEX booking_series_idx ON booking (series_id) WHERE series_id IS NOT NULL;
