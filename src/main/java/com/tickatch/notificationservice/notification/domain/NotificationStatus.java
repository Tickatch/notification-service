package com.tickatch.notificationservice.notification.domain;

public enum NotificationStatus {
  PENDING, // 대기 중
  PROCESSING, // 처리 중
  SENT, // 발송 완료
  FAILED // 발송 실패
}
