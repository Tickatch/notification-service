package com.tickatch.notificationservice.notification.application;

import com.tickatch.notificationservice.notification.domain.Notification;
import com.tickatch.notificationservice.notification.domain.NotificationChannel;

public interface NotificationPublisher {

  /** 알림 발송 메시지 발행 */
  void publish(Notification notification);

  /** 알림 재발송 메시지 발행 */
  default void publishRetry(Notification notification) {
    publish(notification);
  }

  /** 지원하는 채널인지 확인 */
  boolean supports(NotificationChannel channel);
}
