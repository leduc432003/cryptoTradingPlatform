package com.duc.user_service.controller;

import com.duc.user_service.dto.request.ChangePasswordRequest;
import com.duc.user_service.dto.request.UserUpdateRequest;
import com.duc.user_service.kafka.NotificationEvent;
import com.duc.user_service.model.User;
import com.duc.user_service.model.UserRole;
import com.duc.user_service.model.VerificationCode;
import com.duc.user_service.model.VerificationType;
import com.duc.user_service.service.UserService;
import com.duc.user_service.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NewTopic topic;

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);

        return new ResponseEntity<User>(user,HttpStatus.OK);
    }

    @PatchMapping("/enable-two-factor/verify-otp/{otp}")
    public ResponseEntity<User> enableTwoFactorAuthentication(@RequestHeader("Authorization") String jwt, @PathVariable String otp) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
        String sendTo = verificationCode.getVerificationType().equals(VerificationType.EMAIL) ? verificationCode.getEmail() : verificationCode.getMobile();
        boolean isVerified = verificationCode.getOtp().equals(otp);
        if(isVerified) {
            User updateUser = userService.enableTwoFactorAuthentication(verificationCode.getVerificationType(), sendTo, user);
            verificationCodeService.deleteVerificationCodeById(verificationCode);
            return new ResponseEntity<>(updateUser, HttpStatus.OK);
        }
        throw new Exception("otp is wrong");
    }

    @PatchMapping("/disable-two-factor/verify-otp/{otp}")
    public ResponseEntity<User> disableTwoFactorAuthentication(@RequestHeader("Authorization") String jwt, @PathVariable String otp) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
        String sendTo = verificationCode.getVerificationType().equals(VerificationType.EMAIL) ? verificationCode.getEmail() : verificationCode.getMobile();
        boolean isVerified = verificationCode.getOtp().equals(otp);
        if(isVerified) {
            User updateUser = userService.disableTwoFactorAuthentication(verificationCode.getVerificationType(), sendTo, user);
            verificationCodeService.deleteVerificationCodeById(verificationCode);
            return new ResponseEntity<>(updateUser, HttpStatus.OK);
        }
        throw new Exception("otp is wrong");
    }

    @PostMapping("/verification/{verificationType}/send-otp")
    public ResponseEntity<String> sendVerificationOTP(@RequestHeader("Authorization") String jwt, @PathVariable VerificationType verificationType) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());

        if(verificationCode == null) {
            verificationCode = verificationCodeService.sendVerificationCode(user, verificationType);
        }
        if(verificationType.equals(VerificationType.EMAIL)) {
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(user.getEmail())
                    .otp(verificationCode.getOtp())
                    .build();
            kafkaTemplate.send(topic.name(), notificationEvent);
        }

        return new ResponseEntity<>("verification otp sent successfully.",HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestHeader("Authorization") String jwt, @RequestBody ChangePasswordRequest request) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);

        userService.updatePassword(user, request.getOldPassword(), request.getNewPassword());

        return new ResponseEntity<>("changing password is successfully.",HttpStatus.OK);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateUser(@RequestHeader("Authorization") String jwt, @RequestBody UserUpdateRequest request) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);

        User userUpdate = userService.updateUser(user.getId(), request);

        return new ResponseEntity<>(userUpdate, HttpStatus.OK);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<User>> getAllUser(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can watch user list");
        }

        return new ResponseEntity<>(userService.getAllUser(), HttpStatus.OK);
    }

    @GetMapping("/admin/{userId}")
    public ResponseEntity<User> adminGetUserById(@RequestHeader("Authorization") String jwt, @PathVariable Long userId) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can watch user list");
        }

        return new ResponseEntity<>(userService.findUserById(userId), HttpStatus.OK);
    }

    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<Void> deleteUserById(@RequestHeader("Authorization") String jwt, @PathVariable Long userId) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can delete user");
        }

        userService.deleteUserById(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) throws Exception {
        return new ResponseEntity<>(userService.findUserById(userId), HttpStatus.OK);
    }

    @GetMapping("/referral-code/{referralCode}")
    public ResponseEntity<User> getUserByReferralCode(@PathVariable String referralCode) throws Exception {
        return new ResponseEntity<>(userService.getUserByReferralCode(referralCode), HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) throws Exception {
        return new ResponseEntity<>(userService.findUserByEmail(email), HttpStatus.OK);
    }
}
