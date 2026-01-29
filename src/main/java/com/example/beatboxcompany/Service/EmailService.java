package com.example.beatboxcompany.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.*;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    private final String RESEND_API_URL = "https://api.resend.com/emails";
    private final RestTemplate restTemplate = new RestTemplate();
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        // Link này trỏ về trang GitHub Pages của bạn
        String verificationLink = "https://hoangthe8944-star.github.io/boxonline/#/verify?token=" + token;

        Map<String, Object> body = new HashMap<>();
        body.put("from", "BeatBox <onboarding@resend.dev>"); // Resend cho phép dùng email này để test
        body.put("to", Collections.singletonList(toEmail));
        body.put("subject", "[BeatBox] Xác minh tài khoản của bạn");
        body.put("html",
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px;'>"
                        +
                        "<h2 style='color: #008080;'>Chào mừng đến với BeatBox Company!</h2>" +
                        "<p>Cảm ơn bạn đã đăng ký. Để bắt đầu nghe nhạc, vui lòng xác nhận email của bạn bằng cách nhấn vào nút bên dưới:</p>"
                        +
                        "<div style='text-align: center; margin: 30px 0;'>" +
                        "<a href='" + verificationLink
                        + "' style='background-color: #00ced1; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>XÁC MINH NGAY</a>"
                        +
                        "</div>" +
                        "<p style='color: #666; font-size: 12px;'>Link này sẽ hết hạn trong vòng 24 giờ.</p>" +
                        "</div>");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(RESEND_API_URL, entity, String.class);
        } catch (Exception e) {
            System.err.println("Lỗi Resend: " + e.getMessage());
        }
    }

    // Gửi mail liên kết tài khoản
    public void sendLinkEmail(String to, String token) {
        // Link này sẽ gọi trực tiếp về Backend để xử lý confirm
        String link = "https://backend-jfn4.onrender.com/api/auth/link-confirm?token=" + token + "&email=" + to;
        sendRequest(to, "[BeatBox] Liên kết email phụ",
                "Nhấn vào đây để liên kết email này với tài khoản của bạn: " + link);
    }

    private void sendRequest(String to, String subject, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", "BeatBox <onboarding@resend.dev>");
        body.put("to", Collections.singletonList(to));
        body.put("subject", subject);
        body.put("html", "<h3>" + subject + "</h3><p>" + content + "</p>");

        restTemplate.postForEntity(RESEND_API_URL, new HttpEntity<>(body, headers), String.class);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("BeatBox <hoangthe8944@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("[BeatBox] Mã xác thực OTP của bạn");

            String htmlContent = "<div style='font-family: Arial, sans-serif; text-align: center; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    +
                    "   <h2 style='color: #00ced1;'>Xác thực tài khoản BeatBox</h2>" +
                    "   <p>Chào bạn, mã OTP để hoàn tất đăng ký của bạn là:</p>" +
                    "   <h1 style='font-size: 48px; letter-spacing: 10px; color: #333; margin: 20px 0;'>" + otp
                    + "</h1>" +
                    "   <p style='color: #777;'>Mã có hiệu lực trong vòng 5 phút.</p>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println(">>> Đã gửi OTP thành công tới: " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi gửi mail: " + e.getMessage());
        }
    }
}