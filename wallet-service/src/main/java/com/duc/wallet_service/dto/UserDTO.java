package com.duc.wallet_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String mobile;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
    private UserRole role = UserRole.ROLE_CUSTOMER;
    private String referralCode;
    private String referredBy;
    private int referralCount;
}
