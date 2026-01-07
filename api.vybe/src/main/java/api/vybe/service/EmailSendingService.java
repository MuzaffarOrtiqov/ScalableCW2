package api.vybe.service;


import api.vybe.enums.AppLanguage;
import api.vybe.enums.EmailType;
import api.vybe.util.JwtUtil;
import api.vybe.util.RandomUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailSendingService {

    @Value("${spring.mail.username}")
    private String fromAccount;
    @Value("${spring.domain}")
    private String serverDomain;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private EmailHistoryService emailHistoryService;
    @Autowired
    private ResourceBundleMessageService resourceBundleMessageService;
    private Long emailCount = 3l;


 /*   public void sendSimpleEmail(String email, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAccount);
        msg.setTo(email);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }*/

    public void sendMimeMessage(String email, String subject, String text) {
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            msg.setFrom(fromAccount);
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true);
            CompletableFuture.runAsync(() -> {
                mailSender.send(msg);
            });
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    private void checkAndSendMimeEmail(String email, String subject, String text, AppLanguage lang) {
        Long count = emailHistoryService.getEmailCount(email);
        if(count>=emailCount){
            throw new RuntimeException(resourceBundleMessageService.getMessage("sms.too.many",lang));
        }
        sendMimeMessage(email, subject, text);
    }

    public void sendRegistrationEmail(String email, String profileId, String lang) {
        String subject = "Registration Confirmation";
        String body = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Email Verification</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f4f4f4;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 40px auto;\n" +
                "            background: #ffffff;\n" +
                "            padding: 30px;\n" +
                "            border-radius: 12px;\n" +
                "            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .header {\n" +
                "            font-size: 24px;\n" +
                "            font-weight: bold;\n" +
                "            color: #333;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .content {\n" +
                "            font-size: 16px;\n" +
                "            color: #555;\n" +
                "            line-height: 1.6;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .btn {\n" +
                "            display: inline-block;\n" +
                "            background: #007bff;\n" +
                "            color: #ffffff;\n" +
                "            padding: 14px 24px;\n" +
                "            text-decoration: none;\n" +
                "            border-radius: 6px;\n" +
                "            font-size: 18px;\n" +
                "            font-weight: bold;\n" +
                "            transition: background 0.3s ease-in-out;\n" +
                "        }\n" +
                "        .btn:hover {\n" +
                "            background: #0056b3;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            margin-top: 30px;\n" +
                "            font-size: 14px;\n" +
                "            color: #777;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">Verify Your Email</div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Hello,</p>\n" +
                "            <p>Thank you for signing up. Please verify your email address by clicking the button below:</p>\n" +
                "            <a href=\"%s/api/v1/auth/registration/email-verification/%s?lang=%s\" class=\"btn\">Verify Email</a>\n" +
                "            <p>If you did not request this, please ignore this email.</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">&copy; 2025 Your Company Name. All rights reserved.</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";
        body = String.format(body, serverDomain, JwtUtil.encodeProfileId(profileId), lang);
        sendMimeMessage(email, subject, body);
    }

    public void sendPasswordResetEmail(String email, AppLanguage lang) {
        String subject = "Password Reset Request";
        String body = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Password Reset Code</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f4f4f4;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 40px auto;\n" +
                "            background: #ffffff;\n" +
                "            padding: 30px;\n" +
                "            border-radius: 12px;\n" +
                "            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .header {\n" +
                "            font-size: 24px;\n" +
                "            font-weight: bold;\n" +
                "            color: #333;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .content {\n" +
                "            font-size: 16px;\n" +
                "            color: #555;\n" +
                "            line-height: 1.6;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .code-box {\n" +
                "            font-size: 28px;\n" +
                "            font-weight: bold;\n" +
                "            color: #007bff;\n" +
                "            background: #eef4ff;\n" +
                "            padding: 15px;\n" +
                "            display: inline-block;\n" +
                "            border-radius: 8px;\n" +
                "            letter-spacing: 5px;\n" +
                "            margin: 10px 0;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            margin-top: 30px;\n" +
                "            font-size: 14px;\n" +
                "            color: #777;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">Reset Your Password</div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Hello,</p>\n" +
                "            <p>You recently requested to reset your password. Use the verification code below to proceed:</p>\n" +
                "            <div class=\"code-box\">%s</div>\n" +
                "            <p>This code is valid for 10 minutes.</p>\n" +
                "            <p>If you did not request this, please ignore this email.</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">&copy; 2025 Your Company Name. All rights reserved.</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";
        String code = RandomUtil.getRandomSmsCode();
        body = String.format(body, code);
        checkAndSendMimeEmail(email,subject,body,lang);
        emailHistoryService.createEmailHistory(email, subject, body, code, EmailType.RESET_PASSWORD);

    }

    public void sendUsernameChangeConfirmEmail(String email,AppLanguage lang){
        String subject = "Username Change Confirmation";
        String body = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Password Reset Code</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f4f4f4;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 40px auto;\n" +
                "            background: #ffffff;\n" +
                "            padding: 30px;\n" +
                "            border-radius: 12px;\n" +
                "            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .header {\n" +
                "            font-size: 24px;\n" +
                "            font-weight: bold;\n" +
                "            color: #333;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .content {\n" +
                "            font-size: 16px;\n" +
                "            color: #555;\n" +
                "            line-height: 1.6;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .code-box {\n" +
                "            font-size: 28px;\n" +
                "            font-weight: bold;\n" +
                "            color: #007bff;\n" +
                "            background: #eef4ff;\n" +
                "            padding: 15px;\n" +
                "            display: inline-block;\n" +
                "            border-radius: 8px;\n" +
                "            letter-spacing: 5px;\n" +
                "            margin: 10px 0;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            margin-top: 30px;\n" +
                "            font-size: 14px;\n" +
                "            color: #777;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">Reset Your Password</div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Hello,</p>\n" +
                "            <p>You recently requested to change your username. Use the verification code below to proceed:</p>\n" +
                "            <div class=\"code-box\">%s</div>\n" +
                "            <p>This code is valid for 10 minutes.</p>\n" +
                "            <p>If you did not request this, please ignore this email.</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">&copy; 2025 Your Company Name. All rights reserved.</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";
        String code = RandomUtil.getRandomSmsCode();
        body = String.format(body, code);
        checkAndSendMimeEmail(email,subject,body,lang);
        emailHistoryService.createEmailHistory(email, subject, body, code, EmailType.CHANGE_USERNAME);
    }
}
