package com.duc.user_service.dto.request;

import com.duc.user_service.model.UserRole;
import lombok.Data;

@Data
public class AdminCreateUserRequest {
    private String fullName;
    private String email;
    private String mobile;
    private String password;
    private UserRole role;
}
