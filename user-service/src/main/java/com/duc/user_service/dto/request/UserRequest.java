package com.duc.user_service.dto.request;

import com.duc.user_service.model.TwoFactorAuth;
import com.duc.user_service.model.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private Long id;
    private String fullName;
    private String email;
    private String mobile;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String referredBy;
}
