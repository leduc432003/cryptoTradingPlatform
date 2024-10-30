package com.duc.user_service.controller;

import com.duc.user_service.model.User;
import com.duc.user_service.model.VerificationCode;
import com.duc.user_service.model.VerificationType;
import com.duc.user_service.service.EmailService;
import com.duc.user_service.service.UserService;
import com.duc.user_service.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;

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
        throw new Exception("wrong otp");
    }

    @PostMapping("/verification/{verificationType}/send-otp")
    public ResponseEntity<String> sendVerificationOTP(@RequestHeader("Authorization") String jwt, @PathVariable VerificationType verificationType) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());

        if(verificationCode == null) {
            verificationCode = verificationCodeService.sendVerificationCode(user, verificationType);
        }
        if(verificationType.equals(VerificationType.EMAIL)) {
            emailService.sendVerificationOtpEmail(user.getEmail(), verificationCode.getOtp());
        }

        return new ResponseEntity<>("verification otp sent successfully.",HttpStatus.OK);
    }
}
