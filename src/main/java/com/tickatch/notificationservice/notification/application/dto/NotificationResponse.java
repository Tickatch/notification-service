package com.tickatch.notificationservice.notification.application.dto;

import com.tickatch.notificationservice.notification.domain.Notification;
import com.tickatch.notificationservice.notification.domain.NotificationChannel;
import com.tickatch.notificationservice.notification.domain.NotificationStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationResponse(
    Long id,
    UUID userId,
    String eventType,
    NotificationChannel channel,
    String templateCode,
    String subject,
    String recipient,
    NotificationStatus status,
    String errorMessage,
    LocalDateTime requestedAt,
    LocalDateTime sentAt,
    Integer retryCount,
    LocalDateTime createdAt) {
  public static NotificationResponse from(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .userId(notification.getUserId())
        .eventType(notification.getEventType())
        .channel(notification.getChannel())
        .templateCode(notification.getTemplateCode())
        .subject(notification.getSubject())
        .recipient(notification.getRecipient())
        .status(notification.getStatus())
        .errorMessage(notification.getErrorMessage())
        .requestedAt(notification.getCreatedAt())
        .sentAt(notification.getSentAt())
        .retryCount(notification.getRetryCount())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}
