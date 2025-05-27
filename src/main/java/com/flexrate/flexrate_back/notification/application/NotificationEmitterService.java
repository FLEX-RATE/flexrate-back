package com.flexrate.flexrate_back.notification.application;

import com.flexrate.flexrate_back.notification.domain.Notification;
import com.flexrate.flexrate_back.notification.dto.NotificationSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NotificationEmitterService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> heartbeats = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SseEmitter subscribe(Long memberId) {
        log.info("SSE 구독 시작: memberId={}", memberId);

        // 기존 emitter 있으면 종료 및 제거
        SseEmitter existingEmitter = emitters.get(memberId);
        if (existingEmitter != null) {
            log.info("기존 emitter 및 heartbeat 종료: memberId={}", memberId);
            existingEmitter.complete();
            ScheduledFuture<?> existingHeartbeat = heartbeats.remove(memberId);
            if (existingHeartbeat != null) {
                existingHeartbeat.cancel(true);
                log.info("기존 heartbeat 취소 완료: memberId={}", memberId);
            }
            emitters.remove(memberId);
        }

        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        emitters.put(memberId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(memberId);
            ScheduledFuture<?> heartbeat = heartbeats.remove(memberId);
            if (heartbeat != null) heartbeat.cancel(true);
            log.info("Emitter onCompletion 및 heartbeat 취소: memberId={} 제거됨", memberId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(memberId);
            ScheduledFuture<?> heartbeat = heartbeats.remove(memberId);
            if (heartbeat != null) heartbeat.cancel(true);
            log.info("Emitter onTimeout 및 heartbeat 취소: memberId={} 제거됨", memberId);
        });

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
            log.info("connect 이벤트 전송 성공: memberId={}", memberId);
        } catch (IOException e) {
            emitters.remove(memberId);
            log.error("connect 이벤트 전송 실패: memberId={}, error={}", memberId, e.getMessage());
        }

        ScheduledFuture<?> heartbeatFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
                log.debug("heartbeat 전송 성공: memberId={}", memberId);
            } catch (IOException e) {
                log.warn("heartbeat 전송 실패: memberId={}, error={}, heartbeat 취소 중", memberId, e.getMessage());
                emitters.remove(memberId);
                ScheduledFuture<?> heartbeat = heartbeats.remove(memberId);
                if (heartbeat != null) {
                    heartbeat.cancel(true);
                    log.info("heartbeat 취소 완료: memberId={}", memberId);
                }
            }
        }, 10, 10, TimeUnit.SECONDS);

        heartbeats.put(memberId, heartbeatFuture);

        return emitter;
    }

    public void sendNotification(Long memberId, Notification notification) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter != null) {
            try {
                NotificationSummaryDto dto = NotificationSummaryDto.from(notification);
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(dto));
                log.info("notification 이벤트 전송 성공: memberId={}, notificationId={}", memberId, notification.getNotificationId());
            } catch (IOException e) {
                emitters.remove(memberId);
                ScheduledFuture<?> heartbeat = heartbeats.remove(memberId);
                if (heartbeat != null) heartbeat.cancel(true);
                log.warn("notification 이벤트 전송 실패 및 emitter 제거: memberId={}, error={}", memberId, e.getMessage());
            }
        } else {
            log.warn("SSE Emitter 없음: memberId={}", memberId);
        }
    }
}
