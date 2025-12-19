package com.tickatch.notificationservice.notification.application.dto;

import com.tickatch.notificationservice.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record NotificationRequest(
    @NotNull(message = "사용자 ID는 필수입니다.") UUID userId,
    @NotBlank(message = "이벤트 타입은 필수입니다.") String eventType,
    @NotNull(message = "채널은 필수입니다.") NotificationChannel channel,
    @NotBlank(message = "템플릿 코드는 필수입니다.") String templateCode,
    @NotBlank(message = "수신자 정보는 필수입니다.") String recipient,
    @NotNull(message = "템플릿 변수는 필수입니다.") Map<String, Object> templateVariables) {
  public NotificationRequest {
    templateVariables = Map.copyOf(templateVariables);
  }
}
