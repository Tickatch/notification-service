package com.tickatch.notificationservice.notification.infrastructure.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tickatch.notificationservice.notification.domain.Notification;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class SlackSendRequestEvent extends DomainEvent {

  private static final String AGGREGATE_TYPE = "Notification";
  private static final String ROUTING_KEY = "slack.send";

  private final Long notificationId;

  private final String channelId;

  private final String message;

  public SlackSendRequestEvent(Long notificationId, String channelId, String message) {
    super();
    this.notificationId = notificationId;
    this.channelId = channelId;
    this.message = message;
  }

  public static SlackSendRequestEvent from(Notification notification) {
    return new SlackSendRequestEvent(
        notification.getId(), notification.getRecipient(), notification.getContent());
  }

  @JsonCreator
  public SlackSendRequestEvent(
      @JsonProperty("notificationId") Long notificationId,
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("channelId") String channelId,
      @JsonProperty("message") String message) {
    super(eventId, occurredAt, version);
    this.notificationId = notificationId;
    this.channelId = channelId;
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
