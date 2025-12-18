package com.tickatch.notificationservice.global.infrastructure;

import io.github.tickatch.common.util.JsonUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정 클래스.
 *
 * @author 김형섭
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {

  @Value("${messaging.exchange.reservation:tickatch.reservation}")
  private String reservationExchange;

  public static final String QUEUE_RESERVATION_COMPLETED =
      "tickatch.reservation.completed.notification.queue";

  public static final String ROUTING_KEY_RESERVATION_COMPLETED =
      "reservation.completed.notification";

  @Value("${messaging.exchange.ticket:tickatch.ticket}")
  private String ticketExchange;

  public static final String QUEUE_TICKET_ISSUED = "tickatch.ticket.issued.notification.queue";

  public static final String ROUTING_KEY_TICKET_ISSUED = "ticket.issued.notification";

  @Value("${messaging.exchange.notification-sender:tickatch.notification-sender}")
  private String notificationSenderExchange;

  public static final String QUEUE_SEND_RESULT = "tickatch.notification-sender.result.queue";

  public static final String ROUTING_KEY_SEND_RESULT = "notification-sender.result";

  /**
   * 예매 성공 관련 이벤트를 처리하는 Topic Exchange 설정.
   *
   * @return 예매 성공 이벤트용 Exchange
   */
  @Bean
  public TopicExchange reservationExchange() {
    return ExchangeBuilder.topicExchange(reservationExchange).durable(true).build();
  }

  /**
   * 예매 성공 메시지 큐를 생성한다.
   *
   * <p>DLX 설정을 포함하여 실패 시 Dead Letter Queue로 이동할 수 있도록 구성한다.
   *
   * @return 예매 성공용 큐
   */
  @Bean
  public Queue reservationCompletedQueue() {
    return QueueBuilder.durable(QUEUE_RESERVATION_COMPLETED)
        .withArgument("x-dead-letter-exchange", reservationExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_RESERVATION_COMPLETED)
        .build();
  }

  /**
   * 예매 성공 관련 메시지를 큐와 Exchange에 바인딩한다.
   *
   * @param reservationCompletedQueue 예매 성공 큐
   * @param reservationExchange 예매 성공 이벤트 처리 Exchange
   * @return 바인딩 객체
   */
  @Bean
  public Binding reservationCompletedBinding(
      Queue reservationCompletedQueue, TopicExchange reservationExchange) {
    return BindingBuilder.bind(reservationCompletedQueue)
        .to(reservationExchange)
        .with(ROUTING_KEY_RESERVATION_COMPLETED);
  }

  /**
   * 예매 성공 이벤트 Dead Letter Exchange 설정.
   *
   * @return Dead Letter Exchange
   */
  @Bean
  public TopicExchange deadLetterReservationExchange() {
    return ExchangeBuilder.topicExchange(reservationExchange + ".dlx").durable(true).build();
  }

  /**
   * Dead Letter 상태의 예매 성공 메시지를 처리하기 위한 큐 생성.
   *
   * @return Dead Letter Queue
   */
  @Bean
  public Queue deadLetterReservationCompletedQueue() {
    return QueueBuilder.durable(QUEUE_RESERVATION_COMPLETED + ".dlq").build();
  }

  /**
   * 예매 성공 Dead Letter 메시지를 처리하기 위한 바인딩 설정.
   *
   * @param deadLetterReservationCompletedQueue 예매 성공 DLQ 큐
   * @param deadLetterReservationExchange 예매 성공 DLX Exchange
   * @return DLQ 바인딩 객체
   */
  @Bean
  public Binding deadLetterReservationCompletedBinding(
      Queue deadLetterReservationCompletedQueue, TopicExchange deadLetterReservationExchange) {
    return BindingBuilder.bind(deadLetterReservationCompletedQueue)
        .to(deadLetterReservationExchange)
        .with("dlq." + ROUTING_KEY_RESERVATION_COMPLETED);
  }

  /**
   * 티켓 발행 관련 이벤트를 처리하는 Topic Exchange 설정.
   *
   * @return 티켓 발행 이벤트용 Exchange
   */
  @Bean
  public TopicExchange ticketExchange() {
    return ExchangeBuilder.topicExchange(ticketExchange).durable(true).build();
  }

  /**
   * 티켓 발행 메시지 큐를 생성한다.
   *
   * <p>DLX 설정을 포함하여 실패 시 Dead Letter Queue로 이동할 수 있도록 구성한다.
   *
   * @return 티켓 발행용 큐
   */
  @Bean
  public Queue ticketIssuedQueue() {
    return QueueBuilder.durable(QUEUE_TICKET_ISSUED)
        .withArgument("x-dead-letter-exchange", ticketExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_TICKET_ISSUED)
        .build();
  }

  /**
   * 티켓 발행 관련 메시지를 큐와 Exchange에 바인딩한다.
   *
   * @param ticketIssuedQueue 티켓 발행 큐
   * @param reservationExchange 티켓 발행 이벤트 처리 Exchange
   * @return 바인딩 객체
   */
  @Bean
  public Binding ticketIssuedBinding(Queue ticketIssuedQueue, TopicExchange reservationExchange) {
    return BindingBuilder.bind(ticketIssuedQueue)
        .to(reservationExchange)
        .with(ROUTING_KEY_RESERVATION_COMPLETED);
  }

  /**
   * 티켓 발행 이벤트 Dead Letter Exchange 설정.
   *
   * @return Dead Letter Exchange
   */
  @Bean
  public TopicExchange deadLetterTicketExchange() {
    return ExchangeBuilder.topicExchange(ticketExchange + ".dlx").durable(true).build();
  }

  /**
   * Dead Letter 상태의 티켓 발행 메시지를 처리하기 위한 큐 생성.
   *
   * @return Dead Letter Queue
   */
  @Bean
  public Queue deadLetterTicketIssuedQueue() {
    return QueueBuilder.durable(QUEUE_TICKET_ISSUED + ".dlq").build();
  }

  /**
   * 티켓 발행 Dead Letter 메시지를 처리하기 위한 바인딩 설정.
   *
   * @param deadLetterTicketIssuedQueue 티켓 발행 DLQ 큐
   * @param deadLetterReservationExchange 티켓 발행 DLX Exchange
   * @return DLQ 바인딩 객체
   */
  @Bean
  public Binding deadLetterTicketIssuedBinding(
      Queue deadLetterTicketIssuedQueue, TopicExchange deadLetterReservationExchange) {
    return BindingBuilder.bind(deadLetterTicketIssuedQueue)
        .to(deadLetterReservationExchange)
        .with("dlq." + ROUTING_KEY_TICKET_ISSUED);
  }

  /**
   * 알림 발송 결과 관련 이벤트를 처리하는 Topic Exchange 설정.
   *
   * @return 알림 발송 결과 이벤트용 Exchange
   */
  @Bean
  public TopicExchange notificationSenderExchange() {
    return ExchangeBuilder.topicExchange(notificationSenderExchange).durable(true).build();
  }

  /**
   * 알림 발송 결과 메시지 큐를 생성한다.
   *
   * <p>DLX 설정을 포함하여 실패 시 Dead Letter Queue로 이동할 수 있도록 구성한다.
   *
   * @return 알림 발송 결과용 큐
   */
  @Bean
  public Queue sendResultQueue() {
    return QueueBuilder.durable(QUEUE_SEND_RESULT)
        .withArgument("x-dead-letter-exchange", notificationSenderExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_SEND_RESULT)
        .build();
  }

  /**
   * 알림 발송 결과 관련 메시지를 큐와 Exchange에 바인딩한다.
   *
   * @param sendResultQueue 알림 발송 결과 큐
   * @param notificationSenderExchange 알림 발송 결과 이벤트 처리 Exchange
   * @return 바인딩 객체
   */
  @Bean
  public Binding sendResultBinding(
      Queue sendResultQueue, TopicExchange notificationSenderExchange) {
    return BindingBuilder.bind(sendResultQueue)
        .to(notificationSenderExchange)
        .with(ROUTING_KEY_SEND_RESULT);
  }

  /**
   * 알림 발송 결과 이벤트 Dead Letter Exchange 설정.
   *
   * @return Dead Letter Exchange
   */
  @Bean
  public TopicExchange deadLetterNotificationSenderExchange() {
    return ExchangeBuilder.topicExchange(notificationSenderExchange + ".dlx").durable(true).build();
  }

  /**
   * Dead Letter 상태의 알림 발송 결과 메시지를 처리하기 위한 큐 생성.
   *
   * @return Dead Letter Queue
   */
  @Bean
  public Queue deadLetterSendResultQueue() {
    return QueueBuilder.durable(QUEUE_SEND_RESULT + ".dlq").build();
  }

  /**
   * 알림 발송 결과 Dead Letter 메시지를 처리하기 위한 바인딩 설정.
   *
   * @param deadLetterSendResultQueue 알림 발송 결과 DLQ 큐
   * @param deadLetterNotificationSenderExchange 알림 발송 결과 DLX Exchange
   * @return DLQ 바인딩 객체
   */
  @Bean
  public Binding deadLetterSendResultBinding(
      Queue deadLetterSendResultQueue, TopicExchange deadLetterNotificationSenderExchange) {
    return BindingBuilder.bind(deadLetterSendResultQueue)
        .to(deadLetterNotificationSenderExchange)
        .with("dlq." + ROUTING_KEY_SEND_RESULT);
  }

  /**
   * 메시지 직렬화/역직렬화를 위한 JSON 변환기 설정.
   *
   * @return JSON 메시지 컨버터
   */
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter(JsonUtils.getObjectMapper());
  }

  /**
   * RabbitMQ 메시지 송신을 위한 템플릿 설정.
   *
   * @param connectionFactory RabbitMQ 연결 팩토리
   * @param jsonMessageConverter 메시지 직렬화 컨버터
   * @return 구성된 RabbitTemplate
   */
  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter);
    return rabbitTemplate;
  }
}
