package com.tickatch.notificationservice.notification.infrastructure.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tickatch.notificationservice.notification.domain.Notification;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class MmsSendRequestEvent extends DomainEvent {

  private static final String AGGREGATE_TYPE = "Notification";
  private static final String ROUTING_KEY = "mms.send";

  private final Long notificationId;

  private final String phoneNumber;

  private final String message;

  private final String imageBase64;

  public MmsSendRequestEvent(
      Long notificationId, String phoneNumber, String message, String imageBase64) {
    super();
    this.notificationId = notificationId;
    this.phoneNumber = phoneNumber;
    this.message = message;
    this.imageBase64 = imageBase64;
  }

  public static MmsSendRequestEvent from(Notification notification, String imageBase64) {
    return new MmsSendRequestEvent(
        notification.getId(), notification.getRecipient(), notification.getContent(), imageBase64);
  }

  @JsonCreator
  public MmsSendRequestEvent(
      @JsonProperty("notificationId") Long notificationId,
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("phoneNumber") String phoneNumber,
      @JsonProperty("message") String message,
      @JsonProperty("imageBase64") String imageBase64) {
    super(eventId, occurredAt, version);
    this.notificationId = notificationId;
    this.phoneNumber = phoneNumber;
    this.message = message;
    this.imageBase64 = imageBase64;
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
