package com.tickatch.notificationservice.notification.infrastructure.message.publisher;

import com.tickatch.notificationservice.notification.application.NotificationPublisher;
import com.tickatch.notificationservice.notification.domain.Notification;
import com.tickatch.notificationservice.notification.domain.NotificationChannel;
import com.tickatch.notificationservice.notification.infrastructure.message.dto.SmsSendRequestEvent;
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
public class SmsNotificationPublisher implements NotificationPublisher {

  @Value("${spring.application.name:notification-service}")
  private String serviceName;

  private final RabbitTemplate rabbitTemplate;

  @Value("${messaging.sms.exchange:tickatch.sms}")
  private String smsExchange;

  @Override
  public void publish(Notification notification) {
    log.info("SMS 발송 메시지 발행: notificationId={}", notification.getId());

    SmsSendRequestEvent event = SmsSendRequestEvent.from(notification);

    publishEvent(event, notification.getEventType());
  }

  @Override
  public boolean supports(NotificationChannel channel) {
    return channel == NotificationChannel.SMS;
  }

  private void publishEvent(DomainEvent event, String eventDescription) {
    log.info("{} SMS 발송 이벤트 발행 시작: {}", eventDescription, event);

    IntegrationEvent integrationEvent = IntegrationEvent.from(event, serviceName);

    try {
      rabbitTemplate.convertAndSend(smsExchange, event.getRoutingKey(), integrationEvent);
      log.info(
          "{} SMS 발송 이벤트 발행 완료: exchange={}, routingKey={}",
          eventDescription,
          smsExchange,
          event.getRoutingKey());
    } catch (AmqpException e) {
      log.error(
          "{} SMS 발송 이벤트 발행 실패: exchange={}, routingKey={}, event={}",
          eventDescription,
          smsExchange,
          event.getRoutingKey(),
          event,
          e);
      throw e;
    }
  }
}
