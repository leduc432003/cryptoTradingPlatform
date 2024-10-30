package com.duc.user_service.service.impl;

import com.duc.user_service.model.TwoFactorAuth;
import com.duc.user_service.model.User;
import com.duc.user_service.model.VerificationType;
import com.duc.user_service.repository.UserRepository;
import com.duc.user_service.service.JwtService;
import com.duc.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

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
    public User updatePassword(User user, String oldPassword, String newPassword) throws Exception {
        if(oldPassword.equals(user.getPassword())) {
            user.setPassword(newPassword);
            return userRepository.save(user);
        }
        throw new Exception("Old passwrod is wrong");
    }
}
