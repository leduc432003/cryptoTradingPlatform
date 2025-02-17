package com.duc.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class NotificationEvent {
    @Id
    private String id;
    private String channel;
    private String recipient;
    private String subject;
    private Map<String, Object> param;
    private String content;
}
