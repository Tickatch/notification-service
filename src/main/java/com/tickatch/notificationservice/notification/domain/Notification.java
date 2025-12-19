package com.tickatch.notificationservice.notification.domain;

import com.tickatch.notificationservice.global.domain.AbstractTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends AbstractTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 100)
  private String eventType; // RESERVATION_COMPLETED, TICKET_ISSUED, etc.

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationChannel channel; // EMAIL, SMS, SLACK

  @Column(nullable = false)
  private String templateCode;

  @Column(length = 500)
  private String subject;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(columnDefinition = "TEXT")
  private String recipient; // 이메일 주소, 전화번호, Slack ID 등

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationStatus status;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  private LocalDateTime sentAt;

  private Integer retryCount = 0;

  @Column(columnDefinition = "TEXT")
  private String option;

  public static Notification create(
      UUID userId,
      String eventType,
      NotificationChannel channel,
      String templateCode,
      String subject,
      String content,
      String recipient,
      String option) {
    Notification notification = new Notification();

    notification.userId = userId;
    notification.eventType = eventType;
    notification.channel = channel;
    notification.templateCode = templateCode;
    notification.subject = subject;
    notification.content = content;
    notification.recipient = recipient;
    notification.status = NotificationStatus.PENDING;
    notification.option = option;

    return notification;
  }

  public void markAsSent() {
    this.status = NotificationStatus.SENT;
    this.sentAt = LocalDateTime.now();
  }

  public void markAsFailed(String errorMessage) {
    this.status = NotificationStatus.FAILED;
    this.errorMessage = errorMessage;
    this.retryCount++;
  }

  public void markAsProcessing() {
    this.status = NotificationStatus.PROCESSING;
  }

  public boolean canRetry() {
    return this.retryCount < 3 && this.status == NotificationStatus.FAILED;
  }
}
