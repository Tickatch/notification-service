package com.tickatch.notificationservice.notification.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface NotificationRepository extends Repository<Notification, Long> {

  Notification save(Notification notification);

  Optional<Notification> findById(Long id);

  Page<Notification> findAllByUserId(UUID userId, Pageable pageable);

  Page<Notification> findAllByUserIdAndChannel(
      UUID userId, NotificationChannel channel, Pageable pageable);
}
