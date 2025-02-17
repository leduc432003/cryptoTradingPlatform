package com.duc.user_service.dto.request;

import lombok.Data;

@Data
public class NotificationRequest {
    private String eventName;
    private String text;
}
