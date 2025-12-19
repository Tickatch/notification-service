package com.tickatch.notificationservice.notification.infrastructure.message.publisher;

import com.tickatch.notificationservice.notification.application.NotificationPublisher;
import com.tickatch.notificationservice.notification.domain.Notification;
import com.tickatch.notificationservice.notification.domain.NotificationChannel;
import com.tickatch.notificationservice.notification.infrastructure.message.dto.MmsSendRequestEvent;
import io.github.tickatch.common.event.DomainEvent;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MmsNotificationPublisher implements NotificationPublisher {

  @Value("${spring.application.name:notification-service}")
  private String serviceName;

  private final RabbitTemplate rabbitTemplate;

  @Value("${messaging.mms.exchange:tickatch.mms}")
  private String mmsExchange;

  @Override
  public void publish(Notification notification) {
    log.info("MMS 발송 메시지 발행: notificationId={}", notification.getId());
    String imageBase64 = notification.getOption();

    MmsSendRequestEvent event = MmsSendRequestEvent.from(notification, imageBase64);

    publishEvent(event, notification.getEventType());
  }

  @Override
  public boolean supports(NotificationChannel channel) {
    return channel == NotificationChannel.MMS;
  }

  private void publishEvent(DomainEvent event, String eventDescription) {
    log.info("{} MMS 발송 이벤트 발행 시작: {}", eventDescription, event);

    IntegrationEvent integrationEvent = IntegrationEvent.from(event, serviceName);

    try {
      rabbitTemplate.convertAndSend(mmsExchange, event.getRoutingKey(), integrationEvent);
      log.info(
          "{} MMS 발송 이벤트 발행 완료: exchange={}, routingKey={}",
          eventDescription,
          mmsExchange,
          event.getRoutingKey());
    } catch (AmqpException e) {
      log.error(
          "{} MMS 발송 이벤트 발행 실패: exchange={}, routingKey={}, event={}",
          eventDescription,
          mmsExchange,
          event.getRoutingKey(),
          event,
          e);
      throw e;
    }
  }
}
