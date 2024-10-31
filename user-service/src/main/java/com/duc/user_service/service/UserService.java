package com.duc.user_service.service;

import com.duc.user_service.dto.request.UserUpdateRequest;
import com.duc.user_service.model.User;
import com.duc.user_service.model.VerificationType;

public interface UserService {
    User findUserProfileByJwt(String jwt) throws Exception;
    User findUserByEmail(String email) throws Exception;
    User findUserById(Long userId) throws Exception;
    User enableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user);
    User disableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user);
    void updatePassword(User user, String oldPassword, String newPassword) throws Exception;
    void resetPassword(User user, String newPassword) throws Exception;
    User updateUser(Long id, UserUpdateRequest updateUser) throws Exception;
}
