package com.kalalev.booking.api;

import com.kalalev.booking.service.BookingException;
import com.kalalev.booking.service.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns errors into RFC 9457 problem details, for example:
 *
 * <pre>{"title":"Slot unavailable","status":409,"detail":"That time is no longer free. Please pick another slot.",
 * "code":"SLOT_UNAVAILABLE"}</pre>
 *
 * The {@code code} lets clients react without parsing the message. Validation errors come from the parent class.
 */
@RestControllerAdvice(basePackageClasses = ApiExceptionHandler.class)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BookingException.class)
    ProblemDetail booking(BookingException e) {
        HttpStatus status = switch (e.getReason()) {
            case INVALID_TIME -> HttpStatus.UNPROCESSABLE_CONTENT;
            case TOO_MANY_HOLDS -> HttpStatus.TOO_MANY_REQUESTS;
            case SLOT_UNAVAILABLE, HOLD_EXPIRED, CANCELLATION_WINDOW_CLOSED, INVALID_STATE -> HttpStatus.CONFLICT;
        };
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setTitle(title(e.getReason()));
        problem.setProperty("code", e.getReason().name());
        return problem;
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail forbidden(AccessDeniedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "You do not have access to this facility");
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail unauthorized(AuthenticationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Wrong email or password");
    }

    private static String title(BookingException.Reason reason) {
        String words = reason.name().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }
}
