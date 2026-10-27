// 100 customers try to hold the same free slot at the same moment, over HTTP against the running app.
// Expected: exactly one 201, the rest 409 SLOT_UNAVAILABLE.
import http from "k6/http";
import { check } from "k6";
import { Counter } from "k6/metrics";

const BASE = __ENV.BASE || "http://localhost:8080";
const VUS = 100;

const won = new Counter("slot_won");
const lost = new Counter("slot_refused");

export const options = {
    scenarios: {
        rush: { executor: "per-vu-iterations", vus: VUS, iterations: 1, maxDuration: "60s" },
    },
};

export function setup() {
    const tokens = [];
    for (let i = 1; i <= VUS; i++) {
        const res = http.post(
            `${BASE}/api/v1/auth/token`,
            JSON.stringify({ email: `perf${100 + i}@example.com`, password: __ENV.PASSWORD || "demo1234" }),
            { headers: { "Content-Type": "application/json" } },
        );
        tokens.push(res.json("accessToken"));
    }
    // A slot 100 days ahead: after the 90 seeded days, so it is free
    const court = http.get(`${BASE}/api/v1/facilities`).json().find((f) => f.name === "Perf Arena 1").courts[0].id;
    const date = new Date(Date.now() + 100 * 86400000).toISOString().slice(0, 10);
    // Start everyone at the same wall-clock second
    return { tokens, court, date, startAt: Date.now() + 3000 };
}

export default function (data) {
    while (Date.now() < data.startAt) {
        // wait for the common start
    }
    const res = http.post(
        `${BASE}/api/v1/bookings`,
        JSON.stringify({ courtId: data.court, date: data.date, start: "18:00", hours: 2 }),
        { headers: { Authorization: `Bearer ${data.tokens[__VU - 1]}`, "Content-Type": "application/json" } },
    );
    check(res, { "201 or 409": (r) => r.status === 201 || r.status === 409 });
    if (res.status === 201) {
        won.add(1);
    } else if (res.status === 409) {
        lost.add(1);
    }
}
