package com.duc.user_service.service;

import com.duc.user_service.dto.request.AdminCreateUserRequest;
import com.duc.user_service.dto.request.UserUpdateRequest;
import com.duc.user_service.model.User;
import com.duc.user_service.model.VerificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    User findUserProfileByJwt(String jwt) throws Exception;
    User findUserByEmail(String email) throws Exception;
    User findUserById(Long userId) throws Exception;
    User enableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user);
    User disableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user);
    void updatePassword(User user, String oldPassword, String newPassword) throws Exception;
    void resetPassword(User user, String newPassword) throws Exception;
    void deleteUserById(Long userId);
    User updateUser(Long id, UserUpdateRequest updateUser) throws Exception;
    List<User> getAllUser();
    User getUserByReferralCode(String referralCode) throws Exception;
    User adminUpdateUser(Long userId, AdminCreateUserRequest request) throws Exception;
    Page<User> getAllUserPage(Pageable pageable);
}
