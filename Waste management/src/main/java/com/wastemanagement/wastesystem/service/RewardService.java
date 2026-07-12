package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Citizen;
import com.wastemanagement.wastesystem.model.CollectionRecord;
import com.wastemanagement.wastesystem.model.Reward;
import com.wastemanagement.wastesystem.repository.CitizenRepository;
import com.wastemanagement.wastesystem.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles all reward-points crediting logic, keeping Citizen.rewardPoints
 * (the fast-read denormalized total) and the Reward collection (the
 * auditable ledger) in sync — see Reward.java's class-level note for why
 * both exist.
 *
 * Since CollectionRecord.segregationCompliant is a zone-level assessment
 * made by a worker during a single pickup (not a per-household check —
 * see CollectionRecord.java's class-level note), a compliant collection
 * credits a small point award to every citizen registered in that zone,
 * rather than to one specific citizen. This reflects the system's actual
 * collection model: the whole zone is serviced and assessed together.
 */
@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;
    private final CitizenRepository citizenRepository;

    /**
     * Points credited to every citizen in a zone per compliant collection
     * logged. Kept as a simple constant rather than a configurable value
     * for now — the spec doesn't call for tunable point values, and this
     * can be externalized to application.properties later without
     * changing this method's signature (Rule 18).
     */
    private static final int POINTS_PER_COMPLIANT_COLLECTION = 5;

    /**
     * Awards points to every citizen in the collection record's zone.
     * Called by CollectionRecordService immediately after a worker logs
     * a record with segregationCompliant = true.
     *
     * Guards against double-awarding for the same record (e.g. if this
     * were ever accidentally invoked twice) by checking whether a Reward
     * already references this collectionRecordId before crediting again.
     */
    public void awardPointsForCompliantCollection(CollectionRecord record) {
        if (!rewardRepository.findByCollectionRecordId(record.getId()).isEmpty()) {
            return;
        }

        List<Citizen> citizensInZone = citizenRepository.findByZoneId(record.getZoneId());

        for (Citizen citizen : citizensInZone) {
            citizen.setRewardPoints(citizen.getRewardPoints() + POINTS_PER_COMPLIANT_COLLECTION);
            citizenRepository.save(citizen);

            Reward reward = Reward.builder()
                    .citizenId(citizen.getId())
                    .collectionRecordId(record.getId())
                    .points(POINTS_PER_COMPLIANT_COLLECTION)
                    .reason("Proper waste segregation - zone collection on " + record.getCollectionDate())
                    .build();
            rewardRepository.save(reward);
        }
    }

    /**
     * Issues a manual, one-off reward to a single citizen — used by the
     * Super Admin for community recognition bonuses (e.g. Rewards.js
     * "Award Bonus Points" action), as documented on Reward.java.
     */
    public Reward awardManualBonus(String citizenId, int points, String reason, String adminUserId) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found with id: " + citizenId));

        citizen.setRewardPoints(citizen.getRewardPoints() + points);
        citizenRepository.save(citizen);

        Reward reward = Reward.builder()
                .citizenId(citizen.getId())
                .points(points)
                .reason(reason)
                .createdBy(adminUserId)
                .build();

        return rewardRepository.save(reward);
    }

    /**
     * Lists a citizen's full reward history, most recent first — used by
     * the citizen's own Rewards view.
     */
    public List<Reward> getRewardHistoryForCitizen(String citizenId) {
        return rewardRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId);
    }
}