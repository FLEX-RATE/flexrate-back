package com.flexrate.flexrate_back.notification.application;

import com.flexrate.flexrate_back.notification.domain.Notification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NotificationEmitterService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SseEmitter subscribe(Long memberId) {
        log.info("SSE 구독 시작: memberId={}", memberId);
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        emitters.put(memberId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(memberId);
            log.info("Emitter onCompletion: memberId={} 제거됨", memberId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(memberId);
            log.info("Emitter onTimeout: memberId={} 제거됨", memberId);
        });

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
            log.info("connect 이벤트 전송 성공: memberId={}", memberId);
        } catch (IOException e) {
            emitters.remove(memberId);
            log.error("connect 이벤트 전송 실패: memberId={}, error={}", memberId, e.getMessage());
        }

        // 10초마다 heartbeat 전송
        scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
                log.debug("heartbeat 전송 성공: memberId={}", memberId);
            } catch (IOException e) {
                emitters.remove(memberId);
                log.warn("heartbeat 전송 실패 및 emitter 제거: memberId={}, error={}", memberId, e.getMessage());
            }
        }, 10, 10, TimeUnit.SECONDS);

        return emitter;
    }

    public void sendNotification(Long memberId, Notification notification) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
                log.info("notification 이벤트 전송 성공: memberId={}, notificationId={}", memberId, notification.getNotificationId());
            } catch (IOException e) {
                emitters.remove(memberId);
                log.warn("notification 이벤트 전송 실패 및 emitter 제거: memberId={}, error={}", memberId, e.getMessage());
            }
        } else {
            log.warn("SSE Emitter 없음: memberId={}", memberId);
        }
    }
}