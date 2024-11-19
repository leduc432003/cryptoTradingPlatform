package com.duc.notification_service.controller;

import com.duc.notification_service.model.NotificationEvent;
import com.duc.notification_service.service.NotificationConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationConsumer notificationConsumer;

    @GetMapping
    public ResponseEntity<List<NotificationEvent>> getAllNotificationEvents() {
        List<NotificationEvent> list = notificationConsumer.getNotificationEvent();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
