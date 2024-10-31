package com.duc.user_service.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String mobile;
}
