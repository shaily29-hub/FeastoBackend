package com.shailyverma.feasto.email_notification.services;


import com.shailyverma.feasto.email_notification.dtos.NotificationDTO;
import com.shailyverma.feasto.email_notification.entity.Notification;
import com.shailyverma.feasto.email_notification.repository.NotificationRepository;
import com.shailyverma.feasto.enums.NotificationType;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationRepository notificationRepository;


    @Override
    @Async
    public void sendEmail(NotificationDTO notificationDTO) {
        log.info("Sending email to {}", notificationDTO.getRecipient());

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(notificationDTO.getRecipient());
            helper.setSubject(notificationDTO.getSubject());
            helper.setText(notificationDTO.getBody(), notificationDTO.isHtml());
            helper.setFrom("feastodev@gmail.com");

            javaMailSender.send(mimeMessage);

            Notification notificationToSave = Notification.builder()
                    .recipient(notificationDTO.getRecipient())// fixed
                    .subject(notificationDTO.getSubject())
                    .body(notificationDTO.getBody())
                    .type(NotificationType.EMAIL)
                    .isHtml(notificationDTO.isHtml())
                    .build();

            notificationRepository.save(notificationToSave);

            log.info("Saved to notification table");

        } catch (Exception e) {
            log.error("Error sending email", e);
            throw new RuntimeException(e);
        }
    }
}
