package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Reward;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for the "rewards" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed for a citizen's reward history
 * view, Super Admin's reward management screen, and periodic reporting.
 */
@Repository
public interface RewardRepository extends MongoRepository<Reward, String> {

    /**
     * Lists all reward entries for a citizen, most recent first — used by
     * the citizen's "Rewards" history view. Sorting is expressed in the
     * method name so callers don't need to build a Sort object manually
     * for this common case.
     */
    List<Reward> findByCitizenIdOrderByCreatedAtDesc(String citizenId);

    /**
     * Looks up the reward entry generated from a specific collection
     * record, if any — used to prevent double-awarding points if
     * RewardService is invoked more than once for the same record, and to
     * support reversing a reward if a compliance assessment is disputed.
     */
    List<Reward> findByCollectionRecordId(String collectionRecordId);

    /**
     * Lists reward entries created manually by a specific Super Admin
     * (as opposed to system-generated ones) — used for admin activity
     * audit purposes.
     */
    List<Reward> findByCreatedBy(String createdBy);

    /**
     * Lists reward entries within a date range — used by monthly/periodic
     * reward reports (Super Admin's Reports.js) to summarize points issued
     * over a given period without loading a citizen's entire history.
     */
    List<Reward> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}