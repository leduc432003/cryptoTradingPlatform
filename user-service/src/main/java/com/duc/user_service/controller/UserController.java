package com.duc.user_service.controller;

import com.duc.user_service.dto.request.*;
import com.duc.user_service.dto.response.ApiResponse;
import com.duc.user_service.kafka.NotificationEvent;
import com.duc.user_service.model.*;
import com.duc.user_service.repository.UserRepository;
import com.duc.user_service.service.UserService;
import com.duc.user_service.service.VerificationCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NewTopic topic;
    private static final long OTP_EXPIRATION_MINUTES = 5;

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);

        return new ResponseEntity<User>(user,HttpStatus.OK);
    }

    @PatchMapping("/enable-two-factor/verify-otp")
    public ResponseEntity<User> enableTwoFactorAuthentication(@RequestHeader("Authorization") String jwt, @RequestParam String otp) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
        if (verificationCode == null) {
            throw new Exception("No verification code found");
        }

        if (verificationCode.getExpirationTime().isBefore(LocalDateTime.now())) {
            verificationCodeService.deleteVerificationCodeById(verificationCode);
            throw new Exception("OTP has expired");
        }

        if (!verificationCode.getOtp().equals(otp)) {
            throw new Exception("Invalid OTP");
        }

        String sendTo = verificationCode.getVerificationType().equals(VerificationType.EMAIL) ? verificationCode.getEmail() : verificationCode.getMobile();
        User updateUser = userService.enableTwoFactorAuthentication(verificationCode.getVerificationType(), sendTo, user);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }

    @PatchMapping("/disable-two-factor/verify-otp")
    public ResponseEntity<User> disableTwoFactorAuthentication(@RequestHeader("Authorization") String jwt, @RequestParam String otp) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
        if (verificationCode == null) {
            throw new Exception("No verification code found");
        }

        if (verificationCode.getExpirationTime().isBefore(LocalDateTime.now())) {
            verificationCodeService.deleteVerificationCodeById(verificationCode);
            throw new Exception("OTP has expired");
        }

        if (!verificationCode.getOtp().equals(otp)) {
            throw new Exception("Invalid OTP");
        }

        String sendTo = verificationCode.getVerificationType().equals(VerificationType.EMAIL) ? verificationCode.getEmail() : verificationCode.getMobile();
        User updateUser = userService.disableTwoFactorAuthentication(verificationCode.getVerificationType(), sendTo, user);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }

    @PostMapping("/verification/{verificationType}/send-otp")
    public ResponseEntity<String> sendVerificationOTP(@RequestHeader("Authorization") String jwt, @PathVariable VerificationType verificationType) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());

        if (verificationCode != null) {
            verificationCodeService.deleteVerificationCodeById(verificationCode);
        }

        verificationCode = verificationCodeService.sendVerificationCode(
                user,
                verificationType,
                LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES)
        );

        if(verificationType.equals(VerificationType.EMAIL)) {
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(user.getEmail())
                    .content(verificationCode.getOtp())
                    .build();
            kafkaTemplate.send(topic.name(), notificationEvent);
        }

        return new ResponseEntity<>("verification otp sent successfully.",HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestHeader("Authorization") String jwt, @Valid @RequestBody ChangePasswordRequest request) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);

        userService.updatePassword(user, request.getOldPassword(), request.getNewPassword());

        return new ResponseEntity<>("changing password is successfully.",HttpStatus.OK);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateUser(@RequestHeader("Authorization") String jwt, @Valid @RequestBody UserUpdateRequest request) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);

        User userUpdate = userService.updateUser(user.getId(), request);

        return new ResponseEntity<>(userUpdate, HttpStatus.OK);
    }

    @GetMapping("/admin")
    public ResponseEntity<Page<User>> getAllUser(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can watch user list");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return new ResponseEntity<>(userService.getAllUserPage(pageable), HttpStatus.OK);
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

    @PostMapping("/admin")
    public ResponseEntity<User> createUser(@RequestHeader("Authorization") String jwt, @Valid @RequestBody AdminCreateUserRequest request) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can add user");
        }
        User isEmailExist = userRepository.findByEmail(request.getEmail());
        if(isEmailExist != null) {
            throw new Exception("email is already used with another user");
        }

        String referralCode = generateUniqueReferralCode(request.getFullName());
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setMobile(request.getMobile());
        newUser.setAvatar("https://robohash.org/" + request.getFullName() + "?size=200x200");
        newUser.setFullName(request.getFullName());
        newUser.setReferralCode(referralCode);
        newUser.setVerified(true);
        newUser.setRole(request.getRole());
        User saveUser = userRepository.save(newUser);
        return new ResponseEntity<>(saveUser, HttpStatus.CREATED);
    }

    @PutMapping("/admin/{userId}")
    public ResponseEntity<User> adminUpdateUser(@RequestHeader("Authorization") String jwt, @PathVariable Long userId, @Valid @RequestBody AdminUpdateUserRequest request) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can update user");
        }
        User updateUser = userService.adminUpdateUser(userId, request);
        return new ResponseEntity<>(updateUser, HttpStatus.CREATED);
    }

    @PostMapping("/admin/send-notification")
    public ResponseEntity<ApiResponse> sendNotification(@RequestHeader("Authorization") String jwt, @RequestBody NotificationRequest request) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can watch user list");
        }

        List<User> users = userService.getAllUser();
        for(User user1 : users) {
            if(user1.getRole() != UserRole.ROLE_ADMIN) {
                NotificationEvent notificationEvent = NotificationEvent.builder()
                        .channel("EMAIL")
                        .recipient(user1.getEmail())
                        .subject(request.getEventName())
                        .content(request.getText())
                        .build();
                kafkaTemplate.send("notification", notificationEvent);
            }
        }

        ApiResponse response = ApiResponse.builder()
                .message("Send successfully")
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) throws Exception {
        return new ResponseEntity<>(userService.findUserByEmail(email), HttpStatus.OK);
    }

    private String generateReferralCode(String fullName) {
        return fullName.replace(" ", "").toUpperCase() + RandomStringUtils.randomNumeric(4);
    }

    private String generateUniqueReferralCode(String fullName) {
        int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            String referralCode = generateReferralCode(fullName);
            if (!userRepository.existsByReferralCode(referralCode)) {
                return referralCode;
            }
        }
        throw new RuntimeException("Failed to generate a unique referral code after " + maxAttempts + " attempts");
    }
}
