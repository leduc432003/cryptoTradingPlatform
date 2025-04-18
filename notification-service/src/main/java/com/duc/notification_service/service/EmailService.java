package com.duc.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    public void sendVerificationOtpEmail(String email, String otp) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

        String subject = "Verify Your OTP";
        String text = "<div style='font-family: Arial, sans-serif; text-align: center;'>" +
                "<h2 style='color: #333;'>Xác thực tài khoản của bạn</h2>" +
                "<p>Mã xác thực của bạn là:</p>" +
                "<h3 style='color: #4CAF50;'>" + otp + "</h3>" +
                "<p>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>" +
                "</div>";

        try {
            mimeMessageHelper.setFrom("leanhduc04032003@gmail.com");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text, true);

            javaMailSender.send(mimeMessage);
        } catch (MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage());
        } catch (MessagingException e) {
            throw new MessagingException("Error configuring email message: " + e.getMessage());
        }
    }

    public void sendUpcomingEventEmail(String email, String eventName, String eventContent) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

        String subject = "Sự kiện sắp diễn ra: " + eventName;
        String text = "<div style='font-family: Arial, sans-serif; text-align: center;'>" +
                "<h2 style='color: #333;'>Thông báo!</h2>" +
                "<h3 style='color: #4CAF50;'>" + eventName + "</h3>" +
                "<p>" + eventContent + "</p>" +
                "<p>Hãy theo dõi để không bỏ lỡ nhé!</p>" +
                "</div>";

        try {
            mimeMessageHelper.setFrom("leanhduc04032003@gmail.com");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text, true);

            javaMailSender.send(mimeMessage);
        } catch (MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage());
        } catch (MessagingException e) {
            throw new MessagingException("Error configuring email message: " + e.getMessage());
        }
    }
}
