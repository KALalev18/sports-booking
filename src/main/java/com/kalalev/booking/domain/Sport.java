package com.kalalev.booking.domain;

public enum Sport {
    FOOTBALL,
    TENNIS,
    BASKETBALL,
    VOLLEYBALL,
    PADEL;

    public String displayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
