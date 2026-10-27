package com.kalalev.booking.security;

import com.kalalev.booking.domain.Facility;
import com.kalalev.booking.repository.CourtRepository;
import com.kalalev.booking.repository.FacilityRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Used in {@code @PreAuthorize} expressions: a manager may only act on facilities they run, while admins may act on
 * all of them. Example: {@code @PreAuthorize("@facilityAccess.canManageCourt(#courtId, authentication)")}.
 */
@Component("facilityAccess")
public class FacilityAccess {
    private final FacilityRepository facilityRepository;
    private final CourtRepository courtRepository;

    public FacilityAccess(FacilityRepository facilityRepository, CourtRepository courtRepository) {
        this.facilityRepository = facilityRepository;
        this.courtRepository = courtRepository;
    }

    @Transactional(readOnly = true)
    public boolean canManageFacility(long facilityId, Authentication authentication) {
        return facilityRepository
                .findById(facilityId)
                .map(facility -> canManage(facility, authentication))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canManageCourt(long courtId, Authentication authentication) {
        return courtRepository
                .findById(courtId)
                .map(court -> canManage(court.getFacility(), authentication))
                .orElse(false);
    }

    public boolean canManage(Facility facility, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }
        return hasRole(authentication, "ROLE_MANAGER")
                && facility.getManager() != null
                && facility.getManager().getEmail().equals(authentication.getName());
    }

    private static boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
