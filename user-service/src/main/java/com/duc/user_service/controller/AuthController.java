package com.duc.user_service.controller;

import com.duc.user_service.dto.request.LoginRequest;
import com.duc.user_service.dto.response.AuthResponse;
import com.duc.user_service.model.TwoFactorOTP;
import com.duc.user_service.model.User;
import com.duc.user_service.repository.UserRepository;
import com.duc.user_service.service.CustomerUserDetailService;
import com.duc.user_service.service.EmailService;
import com.duc.user_service.service.JwtService;
import com.duc.user_service.service.TwoFactorOTPService;
import com.duc.user_service.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final CustomerUserDetailService customerUserDetailService;
    private final TwoFactorOTPService twoFactorOTPService;
    private final EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) throws Exception {
        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if(isEmailExist != null) {
            throw new Exception(("email is already used with another user"));
        }
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setEmail(user.getEmail());
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

            emailService.sendVerificationOtpEmail(userName, otp);

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
        if(!password.equals(userDetails.getPassword())) {
            throw new BadCredentialsException("invalid password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    public ResponseEntity<AuthResponse> verifySigningOtp(@PathVariable String otp, @RequestParam String id) throws Exception {
        TwoFactorOTP twoFactorOTP = twoFactorOTPService.findById(id);

        if(twoFactorOTPService.verifyTwoFactorOTP(twoFactorOTP, otp)) {
            AuthResponse res = AuthResponse.builder()
                    .message("Two factor authentication verified")
                    .isTwoFactorAuthEnabled(true)
                    .jwt(twoFactorOTP.getJwt())
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        throw new Exception("invalid otp");
    }
}
