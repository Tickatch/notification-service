package com.tickatch.notificationservice.notification.infrastructure.message;

import com.tickatch.notificationservice.notification.infrastructure.message.dto.TicketIssuedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketDeliveryCoordinator {

  private final List<TicketDeliveryStrategy> strategies;

  private static final String BASE_VERIFY_URL = "https://www.tickatch.xyz/ticket/checked?ticketId=";

  /** 수령 방법에 맞는 전략을 찾아 티켓 전송 */
  public void deliverTicket(TicketIssuedEvent event) {
    TicketDeliveryStrategy strategy = findStrategy(event.getReceiveMethod());

    String verifyUrl = buildVerifyUrl(event.getTicketId().toString());

    strategy.deliver(event, verifyUrl);
  }

  private TicketDeliveryStrategy findStrategy(TicketDeliveryMethod method) {
    return strategies.stream()
        .filter(strategy -> strategy.supports(method))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 수령 방법입니다: " + method));
  }

  private String buildVerifyUrl(String ticketId) {
    return BASE_VERIFY_URL + ticketId;
  }
}
