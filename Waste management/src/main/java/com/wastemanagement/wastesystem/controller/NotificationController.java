package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.model.Notification;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Notification feed endpoints, shared identically by all three roles —
 * Super Admin, Worker, and Citizen each only ever see and act on their
 * own notifications. There is deliberately no endpoint to create a
 * notification here; see NotificationService's class-level note on why
 * notifications are always system-generated as a side effect of other
 * actions, never a direct user-facing creation request.
 *
 * Every method resolves the caller's own userId from the JWT-derived
 * SecurityUserPrincipal — a user can only ever see/modify their own
 * notifications, enforced at the service layer (see
 * NotificationService.markAsRead()'s ownership check) as well as here.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications?unreadOnly=true|false
     * Lists the authenticated user's own notifications, most recent
     * first. unreadOnly defaults to false (full feed); set to true for
     * the "unread" filter view on Notifications.js.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationService.getUnreadNotificationsForUser(principal.getUserId())
                : notificationService.getNotificationsForUser(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    /**
     * GET /api/notifications/unread-count
     * Returns the authenticated user's unread notification count — feeds
     * the navbar/sidebar badge without loading the full list.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }

    /**
     * PATCH /api/notifications/{notificationId}/read
     * Marks a single notification as read. NotificationService verifies
     * the notification actually belongs to the requesting user before
     * allowing this.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String notificationId) {
        Notification notification = notificationService.markAsRead(notificationId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    /**
     * PATCH /api/notifications/read-all
     * Marks every one of the authenticated user's unread notifications as
     * read in one action — "Mark all as read" button on Notifications.js.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Object>> markAllAsRead(
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        notificationService.markAllAsRead(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}