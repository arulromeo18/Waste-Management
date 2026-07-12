package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Notification;
import com.wastemanagement.wastesystem.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Creates and serves individual, per-user notifications — see
 * Notification.java's class-level note distinguishing these from
 * admin-authored Announcements.
 *
 * create(...) is called internally by other services as a side effect of
 * their own domain events (e.g. ComplaintService after a status change,
 * RewardService after crediting points, PenaltyService after issuing a
 * penalty) — there is deliberately no public "create notification"
 * endpoint in NotificationController, since a notification is always a
 * system-generated consequence of something else happening, never a
 * direct user action. This mirrors how AuditLogService.log(...) is called
 * by other services rather than exposed as its own endpoint.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Creates a new notification for a single recipient. relatedEntityId
     * and type are both optional — pass null where not applicable (e.g.
     * a general system message with no specific linked entity).
     */
    public Notification create(String userId, String title, String message,
                               String type, String relatedEntityId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .read(false)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Lists all notifications for a user, most recent first — the base
     * query for Notifications.js.
     */
    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Lists only unread notifications for a user, most recent first —
     * used to render the "unread" filter view.
     */
    public List<Notification> getUnreadNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Counts unread notifications for a user — feeds the unread-count
     * badge shown in the navbar/sidebar without loading the full list.
     */
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Marks a single notification as read. Ownership (does this
     * notification actually belong to the requesting user) is verified
     * here rather than left to the controller, since accidentally
     * marking someone else's notification as read would be a real
     * privacy/data-integrity concern, not just a cosmetic bug.
     */
    public Notification markAsRead(String notificationId, String requestingUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUserId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    /**
     * Marks every one of a user's unread notifications as read in one
     * action — "Mark all as read" button on Notifications.js.
     */
    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        for (Notification notification : unread) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
}