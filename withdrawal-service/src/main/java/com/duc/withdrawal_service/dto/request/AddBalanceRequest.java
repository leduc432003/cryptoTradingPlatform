package com.duc.withdrawal_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddBalanceRequest {
    private Long userId;
    private Long money;
}
