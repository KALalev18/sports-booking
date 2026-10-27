package com.kalalev.booking.service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Emails customers about their bookings. Listeners run only after the booking's transaction has committed, so no
 * email goes out for a booking that was rolled back, and they run on another thread, so a slow mail server does not
 * slow down the request. Without spring.mail.host the email is logged instead (handy for local runs and tests).
 */
@Component
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("EEEE d MMMM, HH:mm", Locale.ENGLISH);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public NotificationService(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${booking.mail.from:bookings@sports-booking.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Async
    @TransactionalEventListener
    public void onConfirmed(BookingEvents.Confirmed event) {
        BookingView b = event.booking();
        send(b.customerEmail(), "Booking confirmed: " + b.facilityName(), """
                Hi %s,

                Your booking is confirmed.

                %s, %s (%s)
                %s to %s
                Price: EUR %s

                You can cancel for free until 24 hours before the start.
                """.formatted(
                        b.customerName(),
                        b.facilityName(),
                        b.courtName(),
                        b.sport().displayName(),
                        WHEN.format(b.localStart()),
                        b.localEnd().toLocalTime(),
                        b.price()));
    }

    @Async
    @TransactionalEventListener
    public void onCancelled(BookingEvents.Cancelled event) {
        if (!event.byFacility()) {
            return;
        }
        BookingView b = event.booking();
        send(b.customerEmail(), "Booking cancelled by " + b.facilityName(), """
                Hi %s,

                %s had to cancel your booking on %s, %s. Sorry for the trouble.
                """.formatted(
                        b.customerName(), b.facilityName(), WHEN.format(b.localStart()), b.courtName()));
    }

    void send(String to, String subject, String text) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("Email to {} (no mail server configured): {}\n{}", to, subject, text);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            sender.send(message);
        } catch (MailException e) {
            // The booking itself is already saved; a failed email must not turn into an error for the user
            log.warn("Could not send email to {}: {}", to, e.getMessage());
        }
    }
}
