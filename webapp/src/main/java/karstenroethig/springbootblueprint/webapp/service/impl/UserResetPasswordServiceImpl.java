package karstenroethig.springbootblueprint.webapp.service.impl;

import java.net.URI;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import karstenroethig.springbootblueprint.webapp.config.ApplicationProperties;
import karstenroethig.springbootblueprint.webapp.model.domain.User;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserChangePasswordDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserResetPasswordDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserTokenDto;
import karstenroethig.springbootblueprint.webapp.repository.UserRepository;
import karstenroethig.springbootblueprint.webapp.util.MessageKeyEnum;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationException;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class UserResetPasswordServiceImpl
{
	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private UserServiceImpl userService;
	@Autowired private EmailServiceImpl emailService;
	@Autowired private UserTokenServiceImpl userTokenService;

	@Autowired private PasswordEncoder passwordEncoder;

	@Autowired private UserRepository userRepository;

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
		UserDto userDto = userService.transform(user);

		UserTokenDto changePasswordToken = userTokenService.createChangePasswordToken(userDto);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(applicationProperties.getBaseUrl());
		uriBuilder.path("/auth/change-password");
		uriBuilder.queryParam("token", changePasswordToken.getToken());

		URI changePasswordUri = uriBuilder.build().toUri();

		emailService.sendChangePasswordMessage(userDto, locale, changePasswordUri);
	}

	public UserChangePasswordDto create(String token)
	{
		UserChangePasswordDto userChangePassword = new UserChangePasswordDto();
		userChangePassword.setToken(token);
		return userChangePassword;
	}

	public ValidationResult validate(UserChangePasswordDto userChangePassword)
	{
		ValidationResult result = new ValidationResult();

		if (userChangePassword == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateToken(userChangePassword));

		return result;
	}

	private void checkValidation(UserChangePasswordDto userChangePassword)
	{
		ValidationResult result = validate(userChangePassword);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateToken(UserChangePasswordDto userChangePassword)
	{
		ValidationResult result = new ValidationResult();

		UserTokenDto changePasswordToken = userTokenService.findChangePasswordToken(userChangePassword.getToken());
		if (changePasswordToken == null)
			result.addError("token", MessageKeyEnum.AUTH_CHANGE_PASSWORD_SAVE_ERROR_TOKEN_UNKNOWN);
		else if (changePasswordToken.isExpired())
			result.addError("token", MessageKeyEnum.AUTH_CHANGE_PASSWORD_SAVE_ERROR_TOKEN_EXPIRED);

		return result;
	}

	public void saveNewPassword(UserChangePasswordDto userChangePassword)
	{
		checkValidation(userChangePassword);

		UserTokenDto changePasswordToken = userTokenService.findChangePasswordToken(userChangePassword.getToken());
		User user = userRepository.findById(changePasswordToken.getUser().getId()).orElseThrow();

		user.setHashedPassword(passwordEncoder.encode(userChangePassword.getPassword()));
		userRepository.save(user);

		userTokenService.deleteChangePasswordToken(changePasswordToken.getUser());
	}
}
