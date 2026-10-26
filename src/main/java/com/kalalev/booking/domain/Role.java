package com.kalalev.booking.domain;

public enum Role {
    /** Searches and books courts. */
    CUSTOMER,
    /** Runs one or more facilities: sees their occupancy and blocks slots for maintenance. */
    MANAGER,
    /** Manages users and every facility. */
    ADMIN
}
