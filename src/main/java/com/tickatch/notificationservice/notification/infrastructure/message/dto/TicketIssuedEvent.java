package com.tickatch.notificationservice.notification.infrastructure.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tickatch.notificationservice.notification.infrastructure.message.TicketDeliveryMethod;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TicketIssuedEvent extends DomainEvent {

  private final UUID ticketId;
  private final TicketDeliveryMethod receiveMethod;
  private final UUID reservationId;
  private final String reservationNumber;
  private final UUID reserverId;
  private final String recipient;
  private final String reserverName;
  private final String productName;
  private final LocalDateTime performanceDate;
  private final String artHallName;
  private final String stageName;
  private final String seatNumber;

  public TicketIssuedEvent(
      UUID ticketId,
      TicketDeliveryMethod receiveMethod,
      UUID reservationId,
      String reservationNumber,
      UUID reserverId,
      String recipient,
      String reserverName,
      String productName,
      LocalDateTime performanceDate,
      String artHallName,
      String stageName,
      String seatNumber) {
    super();
    this.ticketId = ticketId;
    this.receiveMethod = receiveMethod;
    this.reservationId = reservationId;
    this.reservationNumber = reservationNumber;
    this.reserverId = reserverId;
    this.recipient = recipient;
    this.reserverName = reserverName;
    this.productName = productName;
    this.performanceDate = performanceDate;
    this.artHallName = artHallName;
    this.stageName = stageName;
    this.seatNumber = seatNumber;
  }

  @JsonCreator
  public TicketIssuedEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("ticketId") UUID ticketId,
      @JsonProperty("receiveMethod") TicketDeliveryMethod receiveMethod,
      @JsonProperty("reservationId") UUID reservationId,
      @JsonProperty("reservationNumber") String reservationNumber,
      @JsonProperty("reserverId") UUID reserverId,
      @JsonProperty("recipient") String recipient,
      @JsonProperty("reserverName") String reserverName,
      @JsonProperty("productName") String productName,
      @JsonProperty("performanceDate") LocalDateTime performanceDate,
      @JsonProperty("artHallName") String artHallName,
      @JsonProperty("stageName") String stageName,
      @JsonProperty("seatNumber") String seatNumber) {
    super(eventId, occurredAt, version);
    this.ticketId = ticketId;
    this.receiveMethod = receiveMethod;
    this.reservationId = reservationId;
    this.reserverId = reserverId;
    this.reservationNumber = reservationNumber;
    this.recipient = recipient;
    this.reserverName = reserverName;
    this.productName = productName;
    this.performanceDate = performanceDate;
    this.artHallName = artHallName;
    this.stageName = stageName;
    this.seatNumber = seatNumber;
  }
}
