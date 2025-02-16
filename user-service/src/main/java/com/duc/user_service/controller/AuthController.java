package com.duc.user_service.controller;

import com.duc.user_service.dto.request.ForgotPasswordRequest;
import com.duc.user_service.dto.request.LoginRequest;
import com.duc.user_service.dto.request.ResetPasswordRequest;
import com.duc.user_service.dto.request.UserRequest;
import com.duc.user_service.dto.response.ApiResponse;
import com.duc.user_service.dto.response.AuthResponse;
import com.duc.user_service.kafka.NotificationEvent;
import com.duc.user_service.model.*;
import com.duc.user_service.repository.UserRepository;
import com.duc.user_service.service.*;
import com.duc.user_service.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CustomerUserDetailService customerUserDetailService;
    private final TwoFactorOTPService twoFactorOTPService;
    private final UserService userService;
    private final ForgotPasswordService forgotPasswordService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NewTopic topic;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> register(@RequestBody UserRequest user) throws Exception {
        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if(isEmailExist != null) {
            throw new Exception("email is already used with another user");
        }
        String referralCode = generateReferralCode(user.getFullName());
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setMobile(user.getMobile());
        newUser.setAvatar("https://robohash.org/" + user.getFullName() + "?size=200x200");
        newUser.setFullName(user.getFullName());
        newUser.setReferralCode(referralCode);
        if (user.getReferredBy() != null) {
            User referrer = userRepository.findByReferralCode(user.getReferredBy());
            if (referrer != null) {
                referrer.setReferralCount(referrer.getReferralCount() + 1);
                userRepository.save(referrer);
            } else {
                throw new Exception("Invalid referral code");
            }
            newUser.setReferredBy(user.getReferredBy());
        }
        User saveUser = userRepository.save(newUser);


        String otp = OtpUtils.generateOtp();
        TwoFactorOTP twoFactorOTP = twoFactorOTPService.createTwoFactorOTP(saveUser, otp, null);

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(saveUser.getEmail())
                .otp(otp)
                .build();
        kafkaTemplate.send(topic.name(), notificationEvent);

        ApiResponse response = ApiResponse.builder()
                .messsage("Registration successful. Please verify your email with the OTP sent.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest) throws Exception {
        String userName = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(loginRequest.getEmail());

        if (user == null) {
            throw new Exception("Invalid email or password");
        }

        if (!user.isVerified()) {
            throw new Exception("Email is not verified. Please check your email.");
        }

        Authentication auth = authenticate(userName, password);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = JwtService.generateToken(auth);


        if(user.getTwoFactorAuth().isEnable()) {
            String otp = OtpUtils.generateOtp();
            TwoFactorOTP oldTwoFactorOTP = twoFactorOTPService.findByUser(user.getId());
            if(oldTwoFactorOTP != null) {
                twoFactorOTPService.deleteTwoFactorOTP(oldTwoFactorOTP);
            }

            TwoFactorOTP newTwoFactorOtp = twoFactorOTPService.createTwoFactorOTP(user, otp, jwt);

            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(user.getEmail())
                    .otp(otp)
                    .build();
            kafkaTemplate.send(topic.name(), notificationEvent);
            AuthResponse res = AuthResponse.builder()
                    .message("Two factor authentication is enable")
                    .isTwoFactorAuthEnabled(true)
                    .session(newTwoFactorOtp.getId())
                    .build();
            return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
        }
        AuthResponse authResponse = AuthResponse.builder()
                .jwt(jwt)
                .status(true)
                .message("login success")
                .build();

        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    private Authentication authenticate(String userName, String password) {
        UserDetails userDetails = customerUserDetailService.loadUserByUsername(userName);
        if(userDetails == null) {
            throw new BadCredentialsException("invalid username");
        }
        if(!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("invalid password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    @PostMapping("/two-factor/otp/{otp}")
    public ResponseEntity<AuthResponse> verifySigningOtp(@PathVariable String otp, @RequestParam String id) throws Exception {
        TwoFactorOTP twoFactorOTP = twoFactorOTPService.findById(id);

        if(twoFactorOTPService.verifyTwoFactorOTP(twoFactorOTP, otp)) {
            AuthResponse res = AuthResponse.builder()
                    .message("Two-factor authentication verified successfully.")
                    .isTwoFactorAuthEnabled(true)
                    .status(true)
                    .jwt(twoFactorOTP.getJwt())
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        throw new Exception("invalid otp");
    }

    @PostMapping("/reset-password/send-otp")
    public ResponseEntity<AuthResponse> sendForgotPasswordOTP(@RequestBody ForgotPasswordRequest request) throws Exception {
        User user =  userService.findUserByEmail(request.getEmail());
        String otp = OtpUtils.generateOtp();
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();
        ForgotPasswordOTP forgotPasswordOTP = forgotPasswordService.findByUser(user.getId());
        if(forgotPasswordOTP == null) {
            forgotPasswordOTP = forgotPasswordService.createOTP(user, id, otp, request.getVerificationType(), request.getEmail());
        }
        if(request.getVerificationType().equals(VerificationType.EMAIL)) {
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(user.getEmail())
                    .otp(otp)
                    .build();
            kafkaTemplate.send(topic.name(), notificationEvent);
        }
        AuthResponse response = AuthResponse.builder()
                .session(forgotPasswordOTP.getId())
                .message("forgot password otp sent successfully.")
                .build();
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PatchMapping("/reset-password/verify-otp/{otp}")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String id, @RequestBody ResetPasswordRequest request, @PathVariable String otp) throws Exception {
        ForgotPasswordOTP forgotPasswordOTP = forgotPasswordService.findById(id);
        boolean isVerified = forgotPasswordOTP.getOtp().equals(otp);
        if(isVerified) {
            userService.resetPassword(forgotPasswordOTP.getUser(), request.getPassword());
            forgotPasswordService.deleteOTP(forgotPasswordOTP);
            ApiResponse res = ApiResponse.builder()
                    .messsage("password is updated successfully.")
                    .build();
            return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
        }
        throw new Exception("otp is wrong");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String email, @RequestParam String otp) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found");
        }

        TwoFactorOTP twoFactorOTP = twoFactorOTPService.findByUser(user.getId());
        if (twoFactorOTP == null || !twoFactorOTP.getOtp().equals(otp)) {
            throw new Exception("Invalid OTP");
        }

        user.setVerified(true);
        userRepository.save(user);

        twoFactorOTPService.deleteTwoFactorOTP(twoFactorOTP);

        ApiResponse response = ApiResponse.builder()
                .messsage("Email verified successfully. You can now log in.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String generateReferralCode(String fullName) {
        return fullName.substring(0, 3).toUpperCase() + RandomStringUtils.randomNumeric(4);
    }
}
