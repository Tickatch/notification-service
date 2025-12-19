package com.tickatch.notificationservice.notification.infrastructure.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tickatch.notificationservice.notification.domain.Notification;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class EmailSendRequestEvent extends DomainEvent {

  private static final String AGGREGATE_TYPE = "Notification";
  private static final String ROUTING_KEY = "email.send";

  private final Long notificationId;

  private final String email;

  private final String subject;

  private final String content;

  private final boolean html;

  public EmailSendRequestEvent(
      Long notificationId, String email, String subject, String content, boolean html) {
    super();
    this.notificationId = notificationId;
    this.email = email;
    this.subject = subject;
    this.content = content;
    this.html = html;
  }

  public static EmailSendRequestEvent from(Notification notification, boolean html) {
    return new EmailSendRequestEvent(
        notification.getId(),
        notification.getRecipient(),
        notification.getSubject(),
        notification.getContent(),
        html);
  }

  @JsonCreator
  public EmailSendRequestEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("notificationId") Long notificationId,
      @JsonProperty("email") String email,
      @JsonProperty("subject") String subject,
      @JsonProperty("content") String content,
      @JsonProperty("html") boolean html) {
    super(eventId, occurredAt, version);
    this.notificationId = notificationId;
    this.email = email;
    this.subject = subject;
    this.content = content;
    this.html = html;
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
