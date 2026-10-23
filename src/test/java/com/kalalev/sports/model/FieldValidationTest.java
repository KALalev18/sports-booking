package com.kalalev.sports.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FieldValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static Field field(LocalTime opens, LocalTime closes) {
        return new Field("Sea Garden Courts", "Varna", "Daily", opens, closes, new BigDecimal("20.00"), null, null);
    }

    @Test
    void acceptsAFacilityThatClosesAfterItOpens() {
        assertThat(validator.validate(field(LocalTime.of(9, 0), LocalTime.of(18, 0)))).isEmpty();
    }

    @Test
    void rejectsAFacilityThatClosesBeforeItOpens() {
        Set<ConstraintViolation<Field>> violations = validator.validate(field(LocalTime.of(18, 0), LocalTime.of(9, 0)));

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Closing time must be after opening time");
    }

    @Test
    void allowsAFreeFacilityWithoutAPrice() {
        Field free = field(LocalTime.of(9, 0), LocalTime.of(18, 0));
        free.setPriceByHour(null);

        assertThat(validator.validate(free)).isEmpty();
    }
}
