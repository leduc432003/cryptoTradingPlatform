package com.duc.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Full name cannot be empty")
    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    private String fullName;
    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10,11}$", message = "Invalid mobile number (must be 10-11 digits)")
    private String mobile;
}
