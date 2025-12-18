package com.tickatch.notificationservice.notification.infrastructure.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tickatch.notificationservice.notification.domain.Notification;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class SmsSendRequestEvent extends DomainEvent {

  private static final String AGGREGATE_TYPE = "Notification";
  private static final String ROUTING_KEY = "sms.send";

  private final Long notificationId;

  private final String phoneNumber;

  private final String message;

  public SmsSendRequestEvent(Long notificationId, String phoneNumber, String message) {
    super();
    this.notificationId = notificationId;
    this.phoneNumber = phoneNumber;
    this.message = message;
  }

  public static SmsSendRequestEvent from(Notification notification) {
    return new SmsSendRequestEvent(
        notification.getId(), notification.getRecipient(), notification.getContent());
  }

  @JsonCreator
  public SmsSendRequestEvent(
      @JsonProperty("notificationId") Long notificationId,
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("phoneNumber") String phoneNumber,
      @JsonProperty("message") String message) {
    super(eventId, occurredAt, version);
    this.notificationId = notificationId;
    this.phoneNumber = phoneNumber;
    this.message = message;
  }

  @Override
  public String getAggregateId() {
    return this.notificationId.toString();
  }

  @Override
  public String getAggregateType() {
    return AGGREGATE_TYPE;
  }

  @Override
  public String getRoutingKey() {
    return ROUTING_KEY;
  }
}
