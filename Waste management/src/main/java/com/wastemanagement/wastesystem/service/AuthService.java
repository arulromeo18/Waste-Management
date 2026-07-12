package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.dto.request.ForgotPasswordRequest;
import com.wastemanagement.wastesystem.dto.request.LoginRequest;
import com.wastemanagement.wastesystem.dto.request.RegisterRequest;
import com.wastemanagement.wastesystem.dto.request.ResetPasswordRequest;
import com.wastemanagement.wastesystem.dto.response.AuthResponse;
import com.wastemanagement.wastesystem.exception.BadRequestException;
import com.wastemanagement.wastesystem.exception.DuplicateResourceException;
import com.wastemanagement.wastesystem.model.Citizen;
import com.wastemanagement.wastesystem.model.PasswordResetOtp;
import com.wastemanagement.wastesystem.model.Role;
import com.wastemanagement.wastesystem.model.User;
import com.wastemanagement.wastesystem.repository.CitizenRepository;
import com.wastemanagement.wastesystem.repository.PasswordResetOtpRepository;
import com.wastemanagement.wastesystem.repository.UserRepository;
import com.wastemanagement.wastesystem.security.JwtService;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Core authentication service handling login, citizen self-registration,
 * and the forgot/reset password OTP flow.
 *
 * Login delegates credential verification to Spring Security's
 * AuthenticationManager (wired with CustomUserDetailsService and
 * BCryptPasswordEncoder in SecurityConfig) rather than manually comparing
 * passwords here — this keeps password verification logic in exactly one
 * place (DaoAuthenticationProvider) instead of duplicating BCrypt-matching
 * code in the service layer.
 *
 * Registration only ever creates CITIZEN accounts (role is hardcoded, never
 * accepted from RegisterRequest) — Worker and Super Admin accounts are
 * provisioned separately by an existing Super Admin via UserService
 * (upcoming), consistent with Worker.java's class-level note.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CitizenRepository citizenRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;

    private static final long OTP_VALIDITY_MINUTES = 10;

    /**
     * Authenticates a user by email/password and issues a JWT on success.
     *
     * Delegates to AuthenticationManager, which internally invokes
     * CustomUserDetailsService to load the user and BCryptPasswordEncoder
     * to compare the submitted password against the stored hash. A
     * BadCredentialsException from Spring Security propagates up to
     * GlobalExceptionHandler (upcoming), which translates it into a clean
     * 401 response rather than leaking internal exception details.
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadRequestException("This account has been deactivated. Please contact the administrator.");
        }

        SecurityUserPrincipal principal = new SecurityUserPrincipal(user);
        String token = jwtService.generateToken(principal, user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * Registers a new citizen account: creates the User (auth/identity)
     * and Citizen (profile) documents together as a pair, then
     * immediately issues a JWT so the citizen is logged in right after
     * signing up without a separate login round-trip.
     *
     * Marked @Transactional so that if the Citizen insert fails after the
     * User insert succeeds, the operation is rolled back rather than
     * leaving an orphaned User with no matching Citizen profile. Note:
     * MongoDB multi-document transactions require a replica set (which
     * MongoDB Atlas provides by default), so this works correctly against
     * the Atlas connection configured in application.properties.
     */
    @Transactional
    public AuthResponse registerCitizen(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CITIZEN)
                .active(true)
                .build();
        user = userRepository.save(user);

        Citizen citizen = Citizen.builder()
                .userId(user.getId())
                .zoneId(request.getZoneId())
                .address(request.getAddress())
                .houseNumber(request.getHouseNumber())
                .landmark(request.getLandmark())
                .pincode(request.getPincode())
                .build();
        citizenRepository.save(citizen);

        SecurityUserPrincipal principal = new SecurityUserPrincipal(user);
        String token = jwtService.generateToken(principal, user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * Initiates the password reset flow: generates a 6-digit OTP, persists
     * it with a 10-minute expiry, and emails it to the user.
     *
     * Always returns normally (no exception) whether or not the email is
     * registered, and the calling controller returns an identical generic
     * success message either way — this deliberately prevents account
     * enumeration, per the security note already flagged on
     * ForgotPasswordRequest.
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String otpCode = otpGenerator.generateNumericOtp(6);

            PasswordResetOtp otp = PasswordResetOtp.builder()
                    .email(user.getEmail())
                    .otpCode(otpCode)
                    .used(false)
                    .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                    .build();
            passwordResetOtpRepository.save(otp);

            emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otpCode, OTP_VALIDITY_MINUTES);
        });
    }

    /**
     * Completes the password reset flow: verifies the submitted OTP is the
     * latest unused one for the email, has not expired, then updates the
     * user's password and marks the OTP as used so it cannot be replayed.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetOtp otp = passwordResetOtpRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        if (!otp.getOtpCode().equals(request.getOtpCode())) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otp.setUsed(true);
        passwordResetOtpRepository.save(otp);
    }
}