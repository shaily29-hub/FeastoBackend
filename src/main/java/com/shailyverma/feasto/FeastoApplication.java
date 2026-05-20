package com.shailyverma.feasto;

import com.shailyverma.feasto.email_notification.dtos.NotificationDTO;
import com.shailyverma.feasto.email_notification.services.NotificationService;
import com.shailyverma.feasto.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.security.crypto.keygen.KeyGenerators.string;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor

public class FeastoApplication {

//	private final NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(FeastoApplication.class, args);
	}

//	@Bean
//	CommandLineRunner runner() {
//		return args -> {
//			NotificationDTO notificationDTO = NotificationDTO.builder()
//					.recipient("feastodev@gmail.com")
//					.subject("Hello Dennis")
//					.body("Hey this is a test email")
//					.type(NotificationType.EMAIL)
//					.build();
//
//			notificationService.sendEmail(notificationDTO);
//		};
//	}
	}
