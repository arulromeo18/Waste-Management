package com.wastemanagement.wastesystem.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates fixed-length numeric OTP (One-Time Password) codes for the
 * password reset flow (see AuthService.forgotPassword() and
 * PasswordResetOtp.java).
 *
 * Uses SecureRandom rather than java.util.Random deliberately — Random is
 * seeded predictably enough (based on system time) that an attacker could
 * theoretically narrow down or predict generated values; SecureRandom
 * draws from a cryptographically strong source, which matters here since
 * an OTP is a security credential (equivalent in sensitivity to a
 * short-lived password) protecting account takeover via password reset.
 *
 * Registered as a Spring @Component (not a static utility class) so it can
 * be injected into AuthService via constructor injection like every other
 * dependency in this codebase, keeping AuthService fully unit-testable
 * (a mock OtpGenerator can be substituted in tests without needing to
 * control static/global state).
 */
@Component
public class OtpGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a numeric-only OTP of the given length (e.g. 6 for
     * AuthService's "123456"-style codes). Each digit is drawn
     * independently and uniformly from 0-9, including allowing a leading
     * zero — the code is always treated as a String, never parsed back
     * into a number, so a leading zero doesn't shorten its effective
     * length or predictability.
     */
    public String generateNumericOtp(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be greater than zero");
        }

        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(secureRandom.nextInt(10));
        }

        return otp.toString();
    }
}