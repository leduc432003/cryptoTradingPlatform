package com.duc.user_service.service.impl;

import com.duc.user_service.dto.request.AdminCreateUserRequest;
import com.duc.user_service.dto.request.AdminUpdateUserRequest;
import com.duc.user_service.dto.request.UserUpdateRequest;
import com.duc.user_service.model.*;
import com.duc.user_service.repository.UserRepository;
import com.duc.user_service.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TwoFactorOTPService twoFactorOTPService;
    private final VerificationCodeService verificationCodeService;
    private final ForgotPasswordService forgotPasswordService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findUserProfileByJwt(String jwt) throws Exception {
        String email = JwtService.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email);

        if(user == null) {
            throw new Exception("user not found");
        }
        return user;
    }

    @Override
    public User findUserByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);

        if(user == null) {
            throw new Exception("user not found");
        }
        return user;
    }

    @Override
    public User findUserById(Long userId) throws Exception {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) {
            throw new Exception("User not found with id " + userId);
        }
        return user.get();
    }

    @Override
    public User enableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user) {
        TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
        twoFactorAuth.setEnable(true);
        twoFactorAuth.setSendTo(verificationType);

        user.setTwoFactorAuth(twoFactorAuth);
        return userRepository.save(user);
    }

    @Override
    public User disableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user) {
        TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
        twoFactorAuth.setEnable(false);
        twoFactorAuth.setSendTo(verificationType);

        user.setTwoFactorAuth(twoFactorAuth);
        return userRepository.save(user);
    }

    @Override
    public void updatePassword(User user, String oldPassword, String newPassword) throws Exception {
        if(passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return;
        }
        throw new Exception("Old password is wrong");
    }

    @Override
    public void resetPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        TwoFactorOTP twoFactorOTP = twoFactorOTPService.findByUser(userId);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(userId);
        ForgotPasswordOTP forgotPasswordOTP = forgotPasswordService.findByUser(userId);
        if(twoFactorOTP != null) {
            twoFactorOTPService.deleteTwoFactorOTP(twoFactorOTP);
        }
        if(verificationCode != null) {
            verificationCodeService.deleteVerificationCodeById(verificationCode);
        }
        if(forgotPasswordOTP != null) {
            forgotPasswordService.deleteOTP(forgotPasswordOTP);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User updateUser(Long id, UserUpdateRequest updateUser) throws Exception {
        User existingUser = findUserById(id);
        if(updateUser.getFullName() != null) {
            existingUser.setFullName(updateUser.getFullName());
        }
        if(updateUser.getMobile() != null) {
            existingUser.setMobile(updateUser.getMobile());
        }
        return userRepository.save(existingUser);
    }

    @Override
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    public User getUserByReferralCode(String referralCode) throws Exception {
        User user = userRepository.findByReferralCode(referralCode);
        if(user == null) {
            throw new Exception("User not found");
        }
        return user;
    }

    @Override
    public User adminUpdateUser(Long userId, AdminUpdateUserRequest request) throws Exception {
        User existingUser = findUserById(userId);
        if(request.getFullName() != null) {
            existingUser.setFullName(request.getFullName());
        }
        if(request.getMobile() != null) {
            existingUser.setMobile(request.getMobile());
        }
        if(request.getRole() != null) {
            existingUser.setRole(request.getRole());
        }
        return userRepository.save(existingUser);
    }

    @Override
    public Page<User> getAllUserPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
