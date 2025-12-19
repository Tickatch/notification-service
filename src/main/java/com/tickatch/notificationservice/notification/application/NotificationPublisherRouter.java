package com.tickatch.notificationservice.notification.application;

import com.tickatch.notificationservice.notification.domain.NotificationChannel;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisherRouter {

  private final List<NotificationPublisher> publishers;

  /** 채널에 맞는 Publisher 반환 */
  public NotificationPublisher getPublisher(NotificationChannel channel) {
    return publishers.stream()
        .filter(publisher -> publisher.supports(channel))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("지원하지 않는 채널입니다: " + channel));
  }
}
