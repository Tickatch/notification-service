package com.tickatch.notificationservice.notification.infrastructure.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class NotificationResultEvent extends DomainEvent {

  private final Long notificationId;
  private final boolean success;
  private final String errorMessage;

  public NotificationResultEvent(Long notificationId, boolean success, String errorMessage) {
    super();
    this.notificationId = notificationId;
    this.success = success;
    this.errorMessage = errorMessage;
  }

  @JsonCreator
  public NotificationResultEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("notificationId") Long notificationId,
      @JsonProperty("success") boolean success,
      @JsonProperty("errorMessage") String errorMessage) {
    super(eventId, occurredAt, version);
    this.notificationId = notificationId;
    this.success = success;
    this.errorMessage = errorMessage;
  }
}
