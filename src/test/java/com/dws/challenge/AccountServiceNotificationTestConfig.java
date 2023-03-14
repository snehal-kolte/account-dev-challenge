package com.dws.challenge;

import com.dws.challenge.service.NotificationService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class AccountServiceNotificationTestConfig {

	@Bean
	@Primary
	public NotificationService notificationService() {
		return Mockito.mock(NotificationService.class);
	}

}
