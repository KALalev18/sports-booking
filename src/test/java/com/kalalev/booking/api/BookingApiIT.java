package com.kalalev.booking.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.kalalev.booking.IntegrationTest;
import com.kalalev.booking.MutableClock;
import com.kalalev.booking.TestData;
import com.kalalev.booking.TestcontainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class BookingApiIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestData data;

    @Autowired
    private MutableClock clock;

    private long court;
    private long facility;

    @BeforeEach
    void setUp() {
        data.reset();
        clock.set(TestcontainersConfig.TEST_START);
        long manager = data.user("manager@example.com", "MANAGER");
        data.user("other.manager@example.com", "MANAGER");
        data.customer("ana@example.com");
        data.customer("boris@example.com");
        facility = data.facility("Sea Garden Courts", "Varna", manager);
        court = data.court(facility, "Court 1", "TENNIS", "20.00");
    }

    private String token(String email) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, TestData.PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }

    private String holdJson(String start, int hours) {
        return "{\"courtId\":%d,\"date\":\"2026-06-02\",\"start\":\"%s\",\"hours\":%d}".formatted(court, start, hours);
    }

    @Test
    void searchIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/availability").param("date", "2026-06-02").param("sport", "TENNIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courtName").value("Court 1"))
                .andExpect(jsonPath("$[0].freeStarts", hasSize(14)));
    }

    @Test
    void holdsAndConfirmsWithABearerToken() throws Exception {
        String ana = token("ana@example.com");
        String body = mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holdJson("18:00", 2)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/bookings/")))
                .andExpect(jsonPath("$.status").value("HELD"))
                .andExpect(jsonPath("$.price").value(40.0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        int id = JsonPath.read(body, "$.id");

        mockMvc.perform(post("/api/v1/bookings/{id}/confirm", id).header("Authorization", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        mockMvc.perform(get("/api/v1/bookings").header("Authorization", ana)).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void aTakenSlotReturns409WithAProblemDetail() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", token("ana@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holdJson("18:00", 1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", token("boris@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holdJson("18:00", 1)))
                .andExpect(status().isConflict())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.code").value("SLOT_UNAVAILABLE"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void invalidRequestsGet400Or422() throws Exception {
        String ana = token("ana@example.com");
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-06-02\",\"start\":\"18:00\",\"hours\":1}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holdJson("23:00", 1)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("INVALID_TIME"));
    }

    @Test
    void requestsWithoutAValidTokenGet401() throws Exception {
        mockMvc.perform(get("/api/v1/bookings")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/bookings").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void managersSeeOnlyTheirOwnFacilities() throws Exception {
        mockMvc.perform(get("/api/v1/manage/facilities/{id}/occupancy", facility)
                        .param("date", "2026-06-02")
                        .header("Authorization", token("manager@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courts", hasSize(1)));

        mockMvc.perform(get("/api/v1/manage/facilities/{id}/occupancy", facility)
                        .param("date", "2026-06-02")
                        .header("Authorization", token("other.manager@example.com")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/manage/facilities").header("Authorization", token("ana@example.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void theOpenApiDocumentIsPublished() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Sports Booking API"))
                .andExpect(jsonPath("$.paths['/api/v1/bookings']").exists());
    }
}
