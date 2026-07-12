package com.wastemanagement.wastesystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Activates MongoDB auditing support across the application.
 *
 * Without this annotation, fields marked with @CreatedDate and @LastModifiedDate
 * (see User.java) are silently ignored by Spring Data MongoDB — they would
 * remain null forever instead of being populated automatically on save/update.
 *
 * This is kept as its own small configuration class (rather than bolted onto
 * WasteSystemApplication or SecurityConfig) so auditing concerns stay
 * independently readable and don't get lost among unrelated bootstrap or
 * security configuration.
 *
 * Any future document (Complaint, CollectionRecord, Announcement, etc.) that
 * declares @CreatedDate / @LastModifiedDate fields will automatically benefit
 * from this single central activation — no per-document wiring needed.
 */
@Configuration
@EnableMongoAuditing
public class AuditingConfig {
}