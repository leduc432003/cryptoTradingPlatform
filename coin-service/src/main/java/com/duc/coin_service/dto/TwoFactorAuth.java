package com.duc.coin_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuth {
    private boolean isEnable = false;
    private VerificationType sendTo;
}
