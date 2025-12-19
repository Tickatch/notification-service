package com.tickatch.notificationservice.notification.infrastructure.message.publisher;

import com.tickatch.notificationservice.notification.application.NotificationPublisher;
import com.tickatch.notificationservice.notification.domain.Notification;
import com.tickatch.notificationservice.notification.domain.NotificationChannel;
import com.tickatch.notificationservice.notification.infrastructure.message.dto.EmailSendRequestEvent;
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
public class EmailNotificationPublisher implements NotificationPublisher {

  @Value("${spring.application.name:notification-service}")
  private String serviceName;

  private final RabbitTemplate rabbitTemplate;

  @Value("${messaging.email.exchange:tickatch.email}")
  private String emailExchange;

  @Override
  public void publish(Notification notification) {
    log.info("이메일 발송 이벤트 발행: notificationId={}", notification.getId());

    EmailSendRequestEvent event = EmailSendRequestEvent.from(notification, true);

    publishEvent(event, notification.getEventType());
  }

  @Override
  public boolean supports(NotificationChannel channel) {
    return channel == NotificationChannel.EMAIL;
  }

  private void publishEvent(DomainEvent event, String eventDescription) {
    log.info("{} 이메일 발송 이벤트 발행 시작: {}", eventDescription, event);

    IntegrationEvent integrationEvent = IntegrationEvent.from(event, serviceName);

    try {
      rabbitTemplate.convertAndSend(emailExchange, event.getRoutingKey(), integrationEvent);
      log.info(
          "{} 이메일 발송 이벤트 발행 완료: exchange={}, routingKey={}",
          eventDescription,
          emailExchange,
          event.getRoutingKey());
    } catch (AmqpException e) {
      log.error(
          "{} 이메일 발송 이벤트 발행 실패: exchange={}, routingKey={}, event={}",
          eventDescription,
          emailExchange,
          event.getRoutingKey(),
          event,
          e);
      throw e;
    }
  }
}
