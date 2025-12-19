package com.tickatch.notificationservice.notification.infrastructure.message;

import com.tickatch.notificationservice.notification.infrastructure.message.dto.TicketIssuedEvent;

// 5. 티켓 전송 전략 인터페이스
public interface TicketDeliveryStrategy {

  /** 티켓 전송 */
  void deliver(TicketIssuedEvent event, String verifyUrl);

  /** 지원하는 수령 방법인지 확인 */
  boolean supports(TicketDeliveryMethod method);
}
