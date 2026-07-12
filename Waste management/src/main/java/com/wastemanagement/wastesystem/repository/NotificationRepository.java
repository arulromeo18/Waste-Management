package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for the "notifications" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed for a user's notification feed
 * (Notifications.js), unread-count badge, and mark-as-read behavior.
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    /**
     * Lists all notifications for a user, most recent first — the base
     * query for the Notifications.js feed.
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Lists only unread notifications for a user, most recent first —
     * used to render the "unread" filter view.
     */
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId);

    /**
     * Counts unread notifications for a user — used for the unread-count
     * badge shown in the navbar/sidebar without loading the full list.
     */
    long countByUserIdAndReadFalse(String userId);
}