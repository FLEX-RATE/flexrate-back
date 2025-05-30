package com.flexrate.flexrate_back.notification.api;

import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.notification.application.NotificationService;
import com.flexrate.flexrate_back.notification.application.NotificationEmitterService;
import com.flexrate.flexrate_back.notification.dto.NotificationResponse;
import com.flexrate.flexrate_back.notification.dto.UnreadCountResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationEmitterService emitterService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(HttpServletResponse response, Principal principal) {
        response.setHeader("X-Accel-Buffering", "no");
        Long memberId = Long.parseLong(principal.getName());

        return emitterService.subscribe(memberId);
    }


    @GetMapping
    public ResponseEntity<NotificationResponse> getNotifications(
            @RequestParam(required = false) Long lastNotificationId,
            Principal principal
    ) {
        Long memberId = Long.parseLong(principal.getName());

        if (lastNotificationId == null) {
            lastNotificationId = Long.MAX_VALUE;
        }

        NotificationResponse response = notificationService.getNotifications(memberId, lastNotificationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll(Principal principal) {
        Long memberId = Long.parseLong(principal.getName());
        notificationService.deleteAll(memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadNotificationCount(Principal principal) {
        Long memberId = Long.parseLong(principal.getName());
        int unreadCount = notificationService.countUnreadNotifications(memberId);
        return ResponseEntity.ok(new UnreadCountResponse(unreadCount));
    }
}