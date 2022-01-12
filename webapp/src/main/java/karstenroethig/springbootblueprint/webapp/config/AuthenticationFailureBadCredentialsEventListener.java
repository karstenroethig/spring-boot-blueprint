package karstenroethig.springbootblueprint.webapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import karstenroethig.springbootblueprint.webapp.service.impl.UserAdminServiceImpl;

@Component
public class AuthenticationFailureBadCredentialsEventListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent>
{
	@Autowired UserAdminServiceImpl userService;

	@Override
	public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event)
	{
		String username = event.getAuthentication().getName();
		userService.incrementFailedLoginAttempts(username);
	}
}
