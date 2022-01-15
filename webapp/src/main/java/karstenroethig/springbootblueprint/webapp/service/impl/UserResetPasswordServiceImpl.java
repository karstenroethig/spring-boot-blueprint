package karstenroethig.springbootblueprint.webapp.service.impl;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import karstenroethig.springbootblueprint.webapp.config.ApplicationProperties;
import karstenroethig.springbootblueprint.webapp.model.domain.PasswordResetToken;
import karstenroethig.springbootblueprint.webapp.model.domain.User;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserResetPasswordDto;
import karstenroethig.springbootblueprint.webapp.repository.PasswordResetTokenRepository;
import karstenroethig.springbootblueprint.webapp.repository.UserRepository;
import karstenroethig.springbootblueprint.webapp.util.MessageKeyEnum;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationException;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class UserResetPasswordServiceImpl
{
	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private OldUserServiceImpl userService;
	@Autowired private EmailServiceImpl emailService;

	@Autowired private UserRepository userRepository;
	@Autowired private PasswordResetTokenRepository passwordResetTokenRepository;

	public UserResetPasswordDto create()
	{
		return new UserResetPasswordDto();
	}

	public ValidationResult validate(UserResetPasswordDto userResetPassword)
	{
		ValidationResult result = new ValidationResult();

		if (userResetPassword == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUser(userResetPassword));

		return result;
	}

	private void checkValidation(UserResetPasswordDto userResetPassword)
	{
		ValidationResult result = validate(userResetPassword);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUser(UserResetPasswordDto userResetPassword)
	{
		ValidationResult result = new ValidationResult();

		User existing = userRepository.findOneByEmailIgnoreCase(userResetPassword.getEmail()).orElse(null);
		if (existing == null)
			result.addError("email", MessageKeyEnum.AUTH_RESET_PASSWORD_SEND_MAIL_ERROR_EMAIL_NOT_FOUND);

		return result;
	}

	public void sendChangePasswordMail(UserResetPasswordDto userResetPassword, Locale locale) throws MessagingException
	{
		checkValidation(userResetPassword);

		User user = userRepository.findOneByEmailIgnoreCase(userResetPassword.getEmail()).orElseThrow();
		String token = UUID.randomUUID().toString();

		PasswordResetToken passwordResetToken = new PasswordResetToken();
		passwordResetToken.setUser(user);
		passwordResetToken.setToken(token);
		passwordResetToken.setExpiredDatetime(LocalDateTime.now().plusDays(1l));

		passwordResetTokenRepository.save(passwordResetToken);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(applicationProperties.getBaseUrl());
		uriBuilder.path("/auth/change-password");
		uriBuilder.queryParam("token", token);

		URI changePasswordUri = uriBuilder.build().toUri();
		UserDto userDto = userService.transform(user);

		emailService.sendChangePasswordMessage(userDto, locale, changePasswordUri);
	}
}
