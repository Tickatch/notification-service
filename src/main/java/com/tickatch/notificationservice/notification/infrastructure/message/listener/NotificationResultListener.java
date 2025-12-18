package com.tickatch.notificationservice.notification.infrastructure.message.listener;

import com.tickatch.notificationservice.global.infrastructure.RabbitMQConfig;
import com.tickatch.notificationservice.notification.application.NotificationService;
import com.tickatch.notificationservice.notification.infrastructure.message.dto.NotificationResultEvent;
import io.github.tickatch.common.event.EventContext;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationResultListener {

  private final NotificationService notificationService;

  @RabbitListener(queues = RabbitMQConfig.QUEUE_SEND_RESULT)
  public void handleNotificationResult(IntegrationEvent event) {
    EventContext.run(event, this::updateSendResult);
  }

  private void updateSendResult(IntegrationEvent event) {
    NotificationResultEvent payload = event.getPayloadAs(NotificationResultEvent.class);
    log.info(
        "알림 발송 결과 수신: notificationId={}, success={}",
        payload.getNotificationId(),
        payload.isSuccess());

    notificationService.updateSendResult(
        payload.getNotificationId(), payload.isSuccess(), payload.getErrorMessage());
  }
}
