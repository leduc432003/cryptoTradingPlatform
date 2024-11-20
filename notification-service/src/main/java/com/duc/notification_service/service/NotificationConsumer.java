package com.duc.notification_service.service;

import com.duc.notification_service.model.NotificationEvent;
import com.duc.notification_service.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationRepository repository;
    private final EmailService emailService;

    @KafkaListener(topics = "send-otp")
    public void consumeLoginNotifications(String notificationEvent) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        NotificationEvent notificationEvent1 = null;
        try {
            notificationEvent1 = objectMapper.readValue(notificationEvent, NotificationEvent.class);
        } catch (Exception e) {
            throw new Exception(e);
        }
        emailService.sendVerificationOtpEmail(notificationEvent1.getRecipient(), notificationEvent1.getOtp());
        repository.save(notificationEvent1);
    }

    public List<NotificationEvent> getNotificationEvent() {
        return repository.findAll();
    }
}
