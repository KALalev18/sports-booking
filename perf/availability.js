// Search load: 20 users repeatedly asking for free slots in one city on a random day.
// Each city has at least 20 facilities and 100 courts after perf/seed.sql.
import http from "k6/http";
import { check } from "k6";

const BASE = __ENV.BASE || "http://localhost:8080";
const CITIES = ["Sofia", "Plovdiv", "Varna", "Burgas", "Ruse", "Stara Zagora", "Pleven", "Sliven", "Dobrich", "Shumen"];

export const options = {
    scenarios: {
        search: { executor: "constant-vus", vus: 20, duration: "60s" },
    },
    summaryTrendStats: ["avg", "med", "p(95)", "p(99)", "max"],
};

function randomDate() {
    const days = 1 + Math.floor(Math.random() * 89);
    return new Date(Date.now() + days * 86400000).toISOString().slice(0, 10);
}

export default function () {
    const city = CITIES[Math.floor(Math.random() * CITIES.length)];
    const res = http.get(`${BASE}/api/v1/availability?date=${randomDate()}&city=${encodeURIComponent(city)}`);
    check(res, {
        "status 200": (r) => r.status === 200,
        "at least 100 courts": (r) => r.json().length >= 100,
    });
}
