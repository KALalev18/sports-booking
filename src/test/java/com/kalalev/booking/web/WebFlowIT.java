package com.kalalev.booking.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kalalev.booking.IntegrationTest;
import com.kalalev.booking.MutableClock;
import com.kalalev.booking.TestData;
import com.kalalev.booking.TestcontainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class WebFlowIT {
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
        data.customer("ana@example.com");
        facility = data.facility("Sea Garden Courts", "Varna", manager);
        court = data.court(facility, "Padel 1", "PADEL", "28.00");
    }

    @Test
    void searchShowsFreeSlotsToEveryone() throws Exception {
        mockMvc.perform(get("/").param("city", "Varna").param("date", "2026-06-02"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sea Garden Courts · Padel 1")))
                .andExpect(content().string(containsString("Log in to book")));
    }

    @Test
    void aCustomerHoldsConfirmsAndCancelsFromTheSite() throws Exception {
        var ana = user("ana@example.com").roles("CUSTOMER");
        String checkout = mockMvc.perform(post("/bookings")
                        .with(ana)
                        .with(csrf())
                        .param("courtId", String.valueOf(court))
                        .param("date", "2026-06-08")
                        .param("start", "18:00")
                        .param("hours", "2"))
                .andExpect(redirectedUrlPattern("/bookings/*"))
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        mockMvc.perform(get(checkout).with(ana))
                .andExpect(content().string(containsString("Confirm your booking")))
                .andExpect(content().string(containsString("€56.00")));
        mockMvc.perform(post(checkout + "/confirm").with(ana).with(csrf())).andExpect(redirectedUrl("/bookings"));
        mockMvc.perform(get("/bookings").with(ana))
                .andExpect(content().string(matchesPattern("(?s).*Padel 1.*Confirmed.*")));
        mockMvc.perform(post(checkout + "/cancel").with(ana).with(csrf()))
                .andExpect(flash().attribute("success", containsString("Cancelled")));
    }

    @Test
    void aTakenSlotSendsTheCustomerBackWithAMessage() throws Exception {
        for (String email : new String[] {"ana@example.com", "manager@example.com"}) {
            mockMvc.perform(post("/bookings")
                    .with(user(email).roles("CUSTOMER"))
                    .with(csrf())
                    .param("courtId", String.valueOf(court))
                    .param("date", "2026-06-08")
                    .param("start", "18:00")
                    .param("back", "/?city=Varna"));
        }
        mockMvc.perform(post("/bookings")
                        .with(user("ana@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .param("courtId", String.valueOf(court))
                        .param("date", "2026-06-08")
                        .param("start", "18:00")
                        .param("back", "//evil.example"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void managersSeeTheDayGridAndCustomersDoNot() throws Exception {
        mockMvc.perform(get("/manage/facilities/{id}", facility)
                        .param("date", "2026-06-02")
                        .with(user("manager@example.com").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("0% booked")));
        mockMvc.perform(get("/manage/facilities/{id}", facility)
                        .with(user("ana@example.com").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }
}
