package com.kalalev.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kalalev.booking.domain.BookingStatus;
import com.kalalev.booking.domain.BookingType;
import com.kalalev.booking.domain.Sport;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class NotificationServiceTest {
    private final JavaMailSender sender = mock(JavaMailSender.class);

    @SuppressWarnings("unchecked")
    private final ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);

    private final NotificationService notifications = new NotificationService(provider, "bookings@example.com");

    private final BookingView booking = new BookingView(
            1L,
            BookingStatus.CONFIRMED,
            BookingType.CUSTOMER,
            2L,
            "Court 1",
            Sport.TENNIS,
            3L,
            "Sea Garden Courts",
            "Varna",
            ZoneId.of("Europe/Sofia"),
            Instant.parse("2026-06-02T15:00:00Z"),
            Instant.parse("2026-06-02T17:00:00Z"),
            new BigDecimal("40.00"),
            null,
            "Ana",
            "ana@example.com",
            null,
            null);

    @Test
    void emailsTheCustomerWhenABookingIsConfirmed() {
        when(provider.getIfAvailable()).thenReturn(sender);

        notifications.onConfirmed(new BookingEvents.Confirmed(booking));

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(message.capture());
        assertThat(message.getValue().getTo()).containsExactly("ana@example.com");
        assertThat(message.getValue().getSubject()).isEqualTo("Booking confirmed: Sea Garden Courts");
        // Shown in the facility's local time, not UTC
        assertThat(message.getValue().getText()).contains("Tuesday 2 June, 18:00 to 20:00", "EUR 40.00");
    }

    @Test
    void onlyEmailsAboutCancellationsTheFacilityMade() {
        when(provider.getIfAvailable()).thenReturn(sender);

        notifications.onCancelled(new BookingEvents.Cancelled(booking, false));
        verify(sender, never()).send(any(SimpleMailMessage.class));

        notifications.onCancelled(new BookingEvents.Cancelled(booking, true));
        verify(sender).send(any(SimpleMailMessage.class));
    }

    @Test
    void aFailingMailServerDoesNotThrow() {
        when(provider.getIfAvailable()).thenReturn(sender);
        doThrow(new MailSendException("down")).when(sender).send(any(SimpleMailMessage.class));

        notifications.onConfirmed(new BookingEvents.Confirmed(booking));
    }
}
