package karstenroethig.springbootblueprint.webapp.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.stereotype.Component;

import karstenroethig.springbootblueprint.webapp.service.impl.UserRegistrationServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
public class AuthenticationFailureDisabledEventListener implements ApplicationListener<AuthenticationFailureDisabledEvent>
{
	@Autowired UserRegistrationServiceImpl userRegistrationService;

	@Override
	public void onApplicationEvent(AuthenticationFailureDisabledEvent event)
	{
		try
		{
			Locale currentLocale = LocaleContextHolder.getLocale();
			String username = event.getAuthentication().getName();
			userRegistrationService.resendRegistrationConfirmMail(username, currentLocale);
		}
		catch (Exception ex)
		{
			log.error("Unable to resend registration confirm mail", ex);
		}
	}
}
