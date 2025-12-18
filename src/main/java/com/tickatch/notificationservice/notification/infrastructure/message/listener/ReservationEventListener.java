package com.tickatch.notificationservice.notification.infrastructure.message.listener;

import com.tickatch.notificationservice.global.infrastructure.RabbitMQConfig;
import com.tickatch.notificationservice.notification.application.NotificationService;
import com.tickatch.notificationservice.notification.application.dto.NotificationRequest;
import com.tickatch.notificationservice.notification.domain.NotificationChannel;
import com.tickatch.notificationservice.notification.infrastructure.message.dto.ReservationCompletedEvent;
import io.github.tickatch.common.event.EventContext;
import io.github.tickatch.common.event.IntegrationEvent;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

  private final NotificationService notificationService;

  private static final String EVENT_TYPE = "RESERVATION_COMPLETED";
  private static final String TEMPLATE_CODE = "RESERVATION_SUCCESS";
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm");

  @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVATION_COMPLETED)
  public void handleReservationCompleted(IntegrationEvent event) {
    EventContext.run(event, this::sendReservationCompletedEmail);
  }

  private void sendReservationCompletedEmail(IntegrationEvent event) {
    ReservationCompletedEvent payload = event.getPayloadAs(ReservationCompletedEvent.class);
    log.info(
        "예매 완료 이벤트 수신: reservationId={}, userId={}",
        payload.getReservationId(),
        payload.getReserverId());

    try {
      // 이메일 알림 발송
      sendEmailNotification(payload);

      log.info("예매 완료 알림 발송 완료: reservationId={}", payload.getReservationId());

    } catch (Exception e) {
      log.error("예매 완료 알림 발송 실패: reservationId={}", payload.getReservationId(), e);
    }
  }

  private void sendEmailNotification(ReservationCompletedEvent event) {
    NotificationRequest request =
        new NotificationRequest(
            event.getReserverId(),
            EVENT_TYPE,
            NotificationChannel.EMAIL,
            TEMPLATE_CODE,
            event.getReserverEmail(),
            buildTemplateVariables(event),
            null);

    notificationService.sendNotification(request);
  }

  private Map<String, Object> buildTemplateVariables(ReservationCompletedEvent event) {
    return Map.of(
        "reservationNumber", event.getReservationNumber(),
        "reserverName", event.getReserverName(),
        "productName", event.getProductName(),
        "performanceDate", event.getPerformanceDate().format(DATE_FORMATTER),
        "artHallName", event.getArtHallName(),
        "stageName", event.getStageName(),
        "seatNumber", event.getSeatNumber());
  }
}
