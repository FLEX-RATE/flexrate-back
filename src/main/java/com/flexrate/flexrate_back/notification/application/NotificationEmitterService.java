package com.flexrate.flexrate_back.notification.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.notification.domain.Notification;
import com.flexrate.flexrate_back.notification.dto.NotificationSummaryDto;
import jakarta.annotation.PreDestroy;
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
    private static final long SSE_TIMEOUT_MILLIS = 60L * 1000 * 60;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> heartbeats = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SseEmitter subscribe(Long memberId) {
        log.info("SSE 구독 시작: memberId={}", memberId);

        // 기존 연결 정리
        cleanupExistingConnection(memberId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        emitters.put(memberId, emitter);

        // 에러 상황 핸들러 설정
        emitter.onCompletion(() -> {
            log.info("Emitter onCompletion: memberId={}", memberId);
            cleanupConnection(memberId);
        });

        emitter.onTimeout(() -> {
            log.info("Emitter onTimeout: memberId={}", memberId);
            cleanupConnection(memberId);
        });

        emitter.onError((throwable) -> {
            log.warn("Emitter onError: memberId={}, error={}", memberId, throwable.getMessage());
            cleanupConnection(memberId);
        });

        // 초기 연결 이벤트 전송
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
            log.info("connect 이벤트 전송 성공:\nmemberId={}", memberId);
        } catch (IOException e) {
            log.error("connect 이벤트 전송 실패:\nmemberId={}, error={}", memberId, e.getMessage());
            cleanupConnection(memberId);
            emitter.completeWithError(e);
        }

        // Heartbeat 스케줄링 (30초로 늘림)
        startHeartbeat(memberId, emitter);

        return emitter;
    }

    private void cleanupExistingConnection(Long memberId) {
        SseEmitter existingEmitter = emitters.get(memberId);
        if (existingEmitter != null) {
            log.info("기존 연결 정리: memberId={}", memberId);
            try {
                existingEmitter.complete();
            } catch (Exception e) {
                log.warn("기존 emitter 완료 중 에러: memberId={}, error={}", memberId, e.getMessage());
            }
            cleanupConnection(memberId);
        }
    }

    private void cleanupConnection(Long memberId) {
        emitters.remove(memberId);
        ScheduledFuture<?> heartbeat = heartbeats.remove(memberId);
        if (heartbeat != null) {
            heartbeat.cancel(true);
            log.info("heartbeat 취소 완료: memberId={}", memberId);
        }
    }

    private void startHeartbeat(Long memberId, SseEmitter emitter) {
        ScheduledFuture<?> heartbeatFuture = scheduler.scheduleAtFixedRate(() -> {
            // Emitter 상태 확인
            if (!emitters.containsKey(memberId)) {
                log.info("Emitter 없음, heartbeat 중단: memberId={}", memberId);
                return;
            }

            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
                log.info("heartbeat 전송 성공:\nmemberId={}", memberId);
            } catch (IOException e) {
                log.warn("heartbeat 전송 실패:\nmemberId={}, error={}", memberId, e.getMessage());
                cleanupConnection(memberId);
                throw new FlexrateException(ErrorCode.NOTIFICATION_HEARTBEAT_FAILED, e);
            } catch (Exception e) {
                log.error("heartbeat 전송 중 예상치 못한 에러: memberId={}, error={}", memberId, e.getMessage());
                cleanupConnection(memberId);
                throw new FlexrateException(ErrorCode.SSE_CONNECTION_ERROR, e);
            }
        }, 30, 30, TimeUnit.SECONDS); // 30초로 변경

        heartbeats.put(memberId, heartbeatFuture);
    }

    public void sendNotification(Long memberId, Notification notification) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter == null) {
            log.info("SSE Emitter 없음: memberId={}", memberId);
            return;
        }

        try {
            NotificationSummaryDto dto = NotificationSummaryDto.from(notification);
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(dto));
            log.info("notification 이벤트 전송 성공:\nmemberId={}, notificationId={}",
                    memberId, notification.getNotificationId());
        } catch (IOException e) {
            log.warn("notification 이벤트 전송 실패:\nmemberId={}, error={}", memberId, e.getMessage());
            cleanupConnection(memberId);
            throw new FlexrateException(ErrorCode.NOTIFICATION_SEND_FAILED, e);
        } catch (Exception e) {
            log.error("notification 전송 중 예상치 못한 에러: memberId={}, error={}", memberId, e.getMessage());
            cleanupConnection(memberId);
            throw new FlexrateException(ErrorCode.SSE_CONNECTION_ERROR, e);
        }
    }

    // 서비스 종료 시 정리
    @PreDestroy
    public void shutdown() {
        log.info("NotificationEmitterService 종료 시작");

        // 모든 연결 정리
        emitters.keySet().forEach(this::cleanupConnection);

        // 스케줄러 종료
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("NotificationEmitterService 종료 완료");
    }
}