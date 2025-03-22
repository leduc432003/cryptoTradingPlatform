package com.duc.user_service.dto.request;

import com.duc.user_service.model.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminCreateUserRequest {
    @NotBlank(message = "Full name cannot be empty")
    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    private String fullName;
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10,11}$", message = "Invalid mobile number (must be 10-11 digits)")
    private String mobile;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
    @NotNull(message = "Role cannot be empty")
    private UserRole role;
}
