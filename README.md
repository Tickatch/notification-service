# notification-service

## 개요
이 프로젝트는 Tickatch의 알림 서비스 프로젝트입니다.
Notification Service는 예매 완료, 티켓 발행 등의 이벤트에 대한 알림을 생성하고, 이메일, SMS, MMS, Slack 등 다양한 채널로 발송을 조율합니다.

> 🚧 **MVP 단계** - 현재 핵심 기능 개발 중입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.x |
| Language | Java 17+ |
| Database | PostgreSQL |
| Messaging | RabbitMQ |
| Template Engine | Thymeleaf |
| Security | Spring Security |
| QR Code | ZXing |

## 아키텍처

### 시스템 구성

```
┌──────────────────────────────────────────────────────────────┐
│                        Tickatch Platform                     │
├─────────────┬─────────────┬──────────────┬───────────────────┤
│ Reservation │   Ticket    │ Notification │ NotificationSender│
│   Service   │   Service   │   Service    │     Service       │
└──────┬──────┴──────┬──────┴───────┬──────┴─────────┬─────────┘
       │             │              │                │
       └─────────────┴──────────────┴────────────────┘
                            │
                      RabbitMQ
                            │
       ┌────────────────────┼────────────────────┐
       ▼                    ▼                    ▼
    Email                 Slack                SMS/MMS
   (SMTP)           (Feign Client)            (SOLAPI)
```

### 레이어 구조

```
notification-service
├── global
│         ├── domain
│         └── infrastructure
├── notification
│         ├── application
│         │         └── dto
│         ├── domain
│         └── infrastructure
│             └── message
│                 ├── dto
│                 ├── listener
│                 └── publisher
└── template
    ├── application
    └── domain
```

## 주요 기능

### 1. 통합 알림 관리
모든 알림을 하나의 Notification 엔티티로 통합 관리하며, 전체 생명주기를 추적합니다.

- **알림 생성**: 이벤트 기반 자동 생성
- **상태 관리**: PENDING → PROCESSING → SENT/FAILED
- **이력 조회**: 사용자별, 채널별 알림 이력 페이징 조회
- **재시도**: 실패 시 최대 3회 자동 재시도

**활용 기술**: Spring Data JPA, Spring Transaction

### 2. 템플릿 기반 알림 생성
템플릿 엔진을 사용하여 동적 알림 콘텐츠를 생성합니다.

- **템플릿 관리**: 이벤트 타입별 템플릿 코드 관리
- **동적 렌더링**: 템플릿 변수 주입을 통한 개인화된 콘텐츠 생성
- **채널별 최적화**: 이메일, SMS, MMS, Slack 각 채널에 최적화된 템플릿

**활용 기술**: Thymeleaf, Template Method Pattern

### 3. 이벤트 기반 알림 트리거
RabbitMQ를 통해 도메인 이벤트를 수신하고 자동으로 알림을 발송합니다.

- **예매 완료 이벤트**: 예매 확정 시 이메일 발송
- **티켓 발행 이벤트**: 티켓 발행 시 이메일/MMS 발송

**활용 기술**: RabbitMQ, Event-Driven Architecture

### 4. 전략 패턴 기반 티켓 배송
수령 방법(이메일/MMS)에 따라 적절한 배송 전략을 선택하여 티켓을 발송합니다.

- **이메일 전략**: HTML 템플릿 + QR 코드 임베드
- **MMS 전략**: 텍스트 메시지 + QR 코드 이미지 첨부
- **QR 코드 생성**: 티켓 검증 URL을 QR 코드로 변환

**활용 기술**: Strategy Pattern, ZXing (QR Code), Base64 Encoding

### 5. 채널별 발행자 라우팅
NotificationPublisherRouter가 알림 채널에 맞는 Publisher를 동적으로 선택합니다.

- **이메일 Publisher**: Email 큐로 메시지 발행
- **Slack Publisher**: Slack 큐로 메시지 발행
- **SMS Publisher**: SMS 큐로 메시지 발행
- **MMS Publisher**: MMS 큐로 메시지 발행

**활용 기술**: Router Pattern, Factory Pattern

### 6. 발송 결과 추적
발송 결과를 수신하여 알림 상태를 업데이트하고 실패 시 재시도를 조율합니다.

- **결과 수신**: NotificationSender로부터 발송 결과 이벤트 수신
- **상태 업데이트**: 성공/실패 상태 및 에러 메시지 기록
- **자동 재시도**: 실패 시 조건에 따라 자동 재발송

**활용 기술**: Event-Driven Architecture, Retry Pattern

## 알림 상태

| 상태 | 설명 |
|------|------|
| PENDING | 알림 생성됨 (발송 대기) |
| PROCESSING | 발송 처리 중 |
| SENT | 발송 완료 |
| FAILED | 발송 실패 |

## 알림 채널

| 채널 | 설명 | 사용 시나리오 |
|------|------|--------------|
| EMAIL | 이메일 | 예매 완료, 티켓 발행 |
| SMS | 단문 문자 | 간단한 알림 |
| MMS | 멀티미디어 문자 | 티켓 발행 (QR 코드 포함) |
| SLACK | Slack 메시지 | 관리자 알림 |

## API 명세

이 서비스는 REST API를 제공하지 않으며, RabbitMQ 메시지를 통해서만 통신합니다.
추후 조회용 API를 제공할 수 있습니다.

## 실행 방법

### 환경 변수

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tickatch
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
  
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html
```

### 실행

```bash
# 개발 환경
./gradlew bootRun

# 프로덕션 빌드
./gradlew clean build
java -jar build/libs/notification-service-*.jar
```

## 데이터베이스 스키마

### p_notification

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | PK |
| user_id | UUID | 사용자 ID |
| event_type | VARCHAR(100) | 이벤트 타입 |
| channel | VARCHAR(20) | 알림 채널 |
| template_code | VARCHAR(50) | 템플릿 코드 |
| subject | VARCHAR(500) | 제목 (이메일용) |
| content | TEXT | 내용 |
| recipient | TEXT | 수신자 정보 |
| status | VARCHAR(20) | 상태 |
| error_message | TEXT | 에러 메시지 |
| sent_at | TIMESTAMP | 발송 일시 |
| retry_count | INTEGER | 재시도 횟수 |
| option | TEXT | 추가 옵션 (QR 코드 등) |
| created_at | TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | 수정일시 |

## 이벤트 명세

### 수신 이벤트

#### ReservationCompletedEvent
- **Payload**:
```json
{
  "reservationId": 123,
  "reserverId": "550e8400-e29b-41d4-a716-446655440000",
  "reserverName": "홍길동",
  "reserverEmail": "user@example.com",
  "reservationNumber": "R20250115001",
  "productName": "오페라의 유령",
  "performanceDate": "2025-02-20T19:00:00",
  "artHallName": "세종문화회관",
  "stageName": "대극장",
  "seatNumber": "A-15"
}
```

#### TicketIssuedEvent
- **Payload**:
```json
{
  "ticketId": 456,
  "reservationId": "550e8400-e29b-41d4-a716-446655440000",
  "reservationNumber": "R20250115001",
  "reserverId": "550e8400-e29b-41d4-a716-446655441234",
  "reserverName": "홍길동",
  "recipient": "user@example.com",
  "receiveMethod": "EMAIL",
  "productName": "오페라의 유령",
  "performanceDate": "2025-02-20T19:00:00",
  "artHallName": "세종문화회관",
  "stageName": "대극장",
  "seatNumber": "A-15"
}
```

#### NotificationResultEvent
- **Payload**:
```json
{
  "notificationId": 1,
  "success": true,
  "errorMessage": null
}
```

### 발행 이벤트

#### EmailSendRequestEvent
- **Payload**:
```json
{
  "notificationId": 1,
  "email": "user@example.com",
  "subject": "예매가 완료되었습니다",
  "content": "<html>...</html>",
  "isHtml": true
}
```

#### MmsSendRequestEvent
- **Payload**:
```json
{
  "notificationId": 4,
  "phoneNumber": "01012345678",
  "message": "티켓이 발행되었습니다",
  "imageBase64": "base64_encoded_image_data"
}
```

## 템플릿 종류

| 템플릿 코드 | 이벤트 타입 | 채널 | 설명 |
|------------|------------|------|------|
| RESERVATION_SUCCESS | RESERVATION_COMPLETED | EMAIL | 예매 완료 이메일 |
| TICKET_ISSUED | TICKET_ISSUED | EMAIL, MMS | 티켓 발행 알림 |

## 관련 서비스

- **Reservation Service** - 예매 완료 이벤트 발행
- **Ticket Service** - 티켓 발행 이벤트 발행
- **NotificationSender Service** - 실제 알림 발송 처리

## 트러블슈팅

### 템플릿 렌더링 실패
- **문제**: 템플릿 변수 누락으로 렌더링 실패
- **해결**: 템플릿 변수 검증 로직 추가 및 기본값 설정

### 재시도 무한 루프
- **문제**: 실패한 알림이 계속 재시도됨
- **해결**: 최대 재시도 횟수(3회) 제한 추가

---

© 2025 Tickatch Team