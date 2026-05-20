package com.shailyverma.feasto.email_notification.services;

import com.shailyverma.feasto.email_notification.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO);
}
