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

  private final boolean isHtml;

  public EmailSendRequestEvent(
      Long notificationId, String email, String subject, String content, boolean isHtml) {
    super();
    this.notificationId = notificationId;
    this.email = email;
    this.subject = subject;
    this.content = content;
    this.isHtml = isHtml;
  }

  public static EmailSendRequestEvent from(Notification notification, boolean isHtml) {
    return new EmailSendRequestEvent(
        notification.getId(),
        notification.getRecipient(),
        notification.getSubject(),
        notification.getContent(),
        isHtml);
  }

  @JsonCreator
  public EmailSendRequestEvent(
      @JsonProperty("notificationId") Long notificationId,
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("email") String email,
      @JsonProperty("subject") String subject,
      @JsonProperty("content") String content,
      @JsonProperty("isHtml") boolean isHtml) {
    super(eventId, occurredAt, version);
    this.notificationId = notificationId;
    this.email = email;
    this.subject = subject;
    this.content = content;
    this.isHtml = isHtml;
  }

  public boolean getIsHtml() {
    return this.isHtml;
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
