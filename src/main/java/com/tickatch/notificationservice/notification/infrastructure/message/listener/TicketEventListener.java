package com.tickatch.notificationservice.notification.infrastructure.message.listener;

import com.tickatch.notificationservice.global.infrastructure.RabbitMQConfig;
import com.tickatch.notificationservice.notification.infrastructure.message.TicketDeliveryCoordinator;
import com.tickatch.notificationservice.notification.infrastructure.message.dto.TicketIssuedEvent;
import io.github.tickatch.common.event.EventContext;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventListener {

  private final TicketDeliveryCoordinator ticketDeliveryCoordinator;

  @RabbitListener(queues = RabbitMQConfig.QUEUE_TICKET_ISSUED)
  public void handleTicketIssued(IntegrationEvent event) {
    EventContext.run(event, this::sendTicketIssuedEmail);
  }

  private void sendTicketIssuedEmail(IntegrationEvent event) {
    TicketIssuedEvent payload = event.getPayloadAs(TicketIssuedEvent.class);
    log.info(
        "티켓 발행 이벤트 수신: ticketId={}, userId={}", payload.getTicketId(), payload.getReserverId());

    try {
      // 이메일 알림 발송
      ticketDeliveryCoordinator.deliverTicket(payload);

      log.info("티켓 발행 알림 발송 완료: ticketId={}", payload.getTicketId());
    } catch (Exception e) {
      log.error("티켓 발행 알림 발송 실패: ticketId={}", payload.getTicketId(), e);
      throw e;
    }
  }
}
