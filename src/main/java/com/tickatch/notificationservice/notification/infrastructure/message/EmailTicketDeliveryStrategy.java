package com.tickatch.notificationservice.notification.infrastructure.message;

import com.tickatch.notificationservice.global.infrastructure.QRCodeService;
import com.tickatch.notificationservice.notification.application.NotificationService;
import com.tickatch.notificationservice.notification.application.dto.NotificationRequest;
import com.tickatch.notificationservice.notification.domain.NotificationChannel;
import com.tickatch.notificationservice.notification.infrastructure.message.dto.TicketIssuedEvent;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTicketDeliveryStrategy implements TicketDeliveryStrategy {

  private final NotificationService notificationService;
  private final QRCodeService qrCodeService;

  private static final String EVENT_TYPE = "TICKET_ISSUED";
  private static final String TEMPLATE_CODE = "TICKET_ISSUED";
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm");

  @Override
  public void deliver(TicketIssuedEvent event, String verifyUrl) {
    String qrCodeBase64 = qrCodeService.generateQRCode(verifyUrl);

    NotificationRequest request =
        new NotificationRequest(
            event.getReservationId(),
            EVENT_TYPE,
            NotificationChannel.EMAIL,
            TEMPLATE_CODE,
            event.getRecipient(),
            buildTemplateVariables(event, verifyUrl, qrCodeBase64),
            qrCodeBase64);

    notificationService.sendNotification(request);
  }

  @Override
  public boolean supports(TicketDeliveryMethod method) {
    return method == TicketDeliveryMethod.EMAIL;
  }

  private Map<String, Object> buildTemplateVariables(
      TicketIssuedEvent event, String verifyUrl, String qrCodeBase64) {
    return Map.of(
        "ticketId", event.getTicketId(),
        "reservationNumber", event.getReservationNumber(),
        "reserverName", event.getReserverName(),
        "productName", event.getProductName(),
        "performanceDate", event.getPerformanceDate().format(DATE_FORMATTER),
        "artHallName", event.getArtHallName(),
        "stageName", event.getStageName(),
        "seatNumber", event.getSeatNumber(),
        "qrCodeImage", qrCodeBase64, // Base64 인코딩된 QR 코드 이미지
        "verifyUrl", verifyUrl);
  }
}
