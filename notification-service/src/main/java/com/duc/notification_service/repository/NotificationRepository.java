package com.duc.notification_service.repository;

import com.duc.notification_service.model.NotificationEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEvent, String> {
}
