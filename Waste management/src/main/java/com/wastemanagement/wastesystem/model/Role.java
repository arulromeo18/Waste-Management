package com.wastemanagement.wastesystem.model;

/**
 * Represents the three distinct user roles supported by the Waste Segregation
 * Monitoring System.
 *
 * This enum is stored as a plain string on the User document (e.g. "SUPER_ADMIN"),
 * embedded as-is in JWT claims (see JwtService.generateToken), and only gets the
 * "ROLE_" prefix applied at the Spring Security boundary in SecurityUserPrincipal
 * (Spring Security's hasRole("X") convention expects an authority named "ROLE_X").
 *
 * Keeping this as an enum (rather than a raw String field) gives compile-time
 * safety everywhere a role is assigned or compared — for example, when a
 * controller checks "is this user a WORKER" or when seeding the initial
 * Super Admin account.
 */
public enum Role {

    /**
     * Full administrative access: manage citizens, workers, vehicles, zones,
     * schedules, complaints, reports, announcements, rewards, and penalties.
     */
    SUPER_ADMIN,

    /**
     * Field staff responsible for collection routes, updating collection
     * status, uploading before/after images, and marking segregation compliance.
     */
    WORKER,

    /**
     * End users who register, submit complaints, view schedules, and earn
     * reward points for proper segregation.
     */
    CITIZEN
}