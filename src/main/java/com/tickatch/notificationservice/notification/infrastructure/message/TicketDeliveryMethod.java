package com.tickatch.notificationservice.notification.infrastructure.message;

import com.tickatch.notificationservice.notification.domain.NotificationChannel;

public enum TicketDeliveryMethod {
  EMAIL,
  MMS;

  public NotificationChannel toNotificationChannel() {
    return switch (this) {
      case EMAIL -> NotificationChannel.EMAIL;
      case MMS -> NotificationChannel.MMS;
    };
  }
}
