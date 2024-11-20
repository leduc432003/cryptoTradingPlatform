package com.duc.user_service.controller;

import com.duc.user_service.dto.request.ForgotPasswordRequest;
import com.duc.user_service.dto.request.LoginRequest;
import com.duc.user_service.dto.request.ResetPasswordRequest;
import com.duc.user_service.dto.response.ApiResponse;
import com.duc.user_service.dto.response.AuthResponse;
import com.duc.user_service.kafka.NotificationEvent;
import com.duc.user_service.model.*;
import com.duc.user_service.repository.UserRepository;
import com.duc.user_service.service.*;
import com.duc.user_service.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<AuthResponse> register(@RequestBody User user) throws Exception {
        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if(isEmailExist != null) {
            throw new Exception("email is already used with another user");
        }
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setMobile(user.getMobile());
        newUser.setAvatar("https://robohash.org/" + user.getFullName() + "?size=200x200");
        newUser.setFullName(user.getFullName());
        User saveUser = userRepository.save(newUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = JwtService.generateToken(auth);

        AuthResponse authResponse = AuthResponse.builder()
                .jwt(jwt)
                .status(true)
                .message("register success")
                .build();

        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest) throws Exception {
        String userName = loginRequest.getEmail();
        String password = loginRequest.getPassword();


        Authentication auth = authenticate(userName, password);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = JwtService.generateToken(auth);

        User user = userRepository.findByEmail(loginRequest.getEmail());

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
}
