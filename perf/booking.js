// Booking load: 50 customers holding random slots and releasing them again.
// A hold is either created (201) or refused because the slot is taken (409); both are correct answers.
// Each successful hold is released so customers stay under the 3-active-holds limit.
import http from "k6/http";
import { check } from "k6";
import { Counter, Trend } from "k6/metrics";

const BASE = __ENV.BASE || "http://localhost:8080";
const VUS = 50;

const created = new Counter("holds_created");
const refused = new Counter("holds_refused_slot_taken");
const holdTime = new Trend("hold_request_duration", true);

export const options = {
    scenarios: {
        book: { executor: "constant-vus", vus: VUS, duration: "60s" },
    },
    summaryTrendStats: ["avg", "med", "p(95)", "p(99)", "max"],
    thresholds: { checks: ["rate==1"] },
};

export function setup() {
    const tokens = [];
    for (let i = 1; i <= VUS; i++) {
        const res = http.post(
            `${BASE}/api/v1/auth/token`,
            JSON.stringify({ email: `perf${i}@example.com`, password: __ENV.PASSWORD || "demo1234" }),
            { headers: { "Content-Type": "application/json" } },
        );
        tokens.push(res.json("accessToken"));
    }
    const courts = http
        .get(`${BASE}/api/v1/facilities`)
        .json()
        .filter((f) => f.name.startsWith("Perf Arena"))
        .flatMap((f) => f.courts.map((c) => c.id));
    return { tokens, courts };
}

export default function (data) {
    const auth = { headers: { Authorization: `Bearer ${data.tokens[__VU - 1]}`, "Content-Type": "application/json" } };
    const days = 1 + Math.floor(Math.random() * 89);
    const body = JSON.stringify({
        courtId: data.courts[Math.floor(Math.random() * data.courts.length)],
        date: new Date(Date.now() + days * 86400000).toISOString().slice(0, 10),
        start: `${String(8 + Math.floor(Math.random() * 14)).padStart(2, "0")}:00`,
        hours: 1,
    });

    const res = http.post(`${BASE}/api/v1/bookings`, body, auth);
    holdTime.add(res.timings.duration);
    check(res, { "201 held or 409 taken": (r) => r.status === 201 || r.status === 409 });
    if (res.status === 201) {
        created.add(1);
        const release = http.post(`${BASE}/api/v1/bookings/${res.json("id")}/cancel`, null, auth);
        check(release, { "released": (r) => r.status === 200 });
    } else {
        refused.add(1);
    }
}
