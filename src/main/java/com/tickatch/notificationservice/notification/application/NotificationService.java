package com.tickatch.notificationservice.notification.application;

import com.tickatch.notificationservice.notification.application.dto.NotificationRequest;
import com.tickatch.notificationservice.notification.application.dto.NotificationResponse;
import com.tickatch.notificationservice.notification.domain.Notification;
import com.tickatch.notificationservice.notification.domain.NotificationChannel;
import com.tickatch.notificationservice.notification.domain.NotificationRepository;
import com.tickatch.notificationservice.template.application.TemplateService;
import com.tickatch.notificationservice.template.domain.TemplateType;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final TemplateService templateService;
  private final NotificationPublisherRouter notificationPublisherRouter;

  /** 알림 생성 및 발송 요청 */
  @Transactional
  public void sendNotification(@Valid NotificationRequest request) {
    log.info(
        "알림 발송 요청: userId={}, eventType={}, channel={}",
        request.userId(),
        request.eventType(),
        request.channel());

    // 1. 템플릿 렌더링
    String subject = renderSubject(request);
    String content = renderContent(request);

    // 2. 알림 엔티티 생성
    Notification notification =
        Notification.create(
            request.userId(),
            request.eventType(),
            request.channel(),
            request.templateCode(),
            subject,
            content,
            request.recipient(),
            request.option());

    Notification saved = notificationRepository.save(notification);
    log.info("알림 생성 완료: notificationId={}", saved.getId());

    // 3. 발송 처리 상태로 변경
    saved.markAsProcessing();

    // 4. 발송 서비스로 메시지 발행
    publishNotification(saved);
  }

  /** 발송 결과 업데이트 */
  @Transactional
  public void updateSendResult(Long notificationId, boolean success, String errorMessage) {
    Notification notification = getNotification(notificationId);

    if (success) {
      notification.markAsSent();
      log.info("알림 발송 성공: notificationId={}", notificationId);
    } else {
      notification.markAsFailed(errorMessage);
      log.warn(
          "알림 발송 실패: notificationId={}, retryCount={}",
          notificationId,
          notification.getRetryCount());

      if (notification.canRetry()) {
        log.info("알림 재발송 요청: notificationId={}", notificationId);
        retryNotification(notification);
      }
    }
  }

  /** 사용자의 알림 목록 조회 */
  @Transactional(readOnly = true)
  public Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
    return notificationRepository.findAllByUserId(userId, pageable).map(NotificationResponse::from);
  }

  /** 사용자의 채널별 알림 조회 */
  @Transactional(readOnly = true)
  public Page<NotificationResponse> getNotificationsByChannel(
      UUID userId, NotificationChannel channel, Pageable pageable) {

    return notificationRepository
        .findAllByUserIdAndChannel(userId, channel, pageable)
        .map(NotificationResponse::from);
  }

  /** 알림 상세 조회 */
  @Transactional(readOnly = true)
  public Notification getNotification(Long notificationId) {
    return notificationRepository
        .findById(notificationId)
        .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));
  }

  // Private helper methods

  private String renderSubject(NotificationRequest request) {
    if (request.channel() != NotificationChannel.EMAIL) {
      return null;
    }

    return templateService.renderEmailSubject(request.templateCode(), request.templateVariables());
  }

  private String renderContent(NotificationRequest request) {
    TemplateType templateType = convertChannelToTemplateType(request.channel());

    return templateService.renderTemplate(
        request.templateCode(), templateType, request.templateVariables());
  }

  private TemplateType convertChannelToTemplateType(NotificationChannel channel) {
    return switch (channel) {
      case EMAIL -> TemplateType.EMAIL;
      case SMS -> TemplateType.SMS;
      case MMS -> TemplateType.MMS;
      case SLACK -> TemplateType.SLACK;
    };
  }

  private void publishNotification(Notification notification) {
    NotificationPublisher publisher =
        notificationPublisherRouter.getPublisher(notification.getChannel());
    publisher.publish(notification);
  }

  private void retryNotification(Notification notification) {
    NotificationPublisher publisher =
        notificationPublisherRouter.getPublisher(notification.getChannel());
    publisher.publishRetry(notification);
  }
}
