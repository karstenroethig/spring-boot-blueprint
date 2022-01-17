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
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserRegistrationDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserTokenDto;
import karstenroethig.springbootblueprint.webapp.repository.UserRepository;
import karstenroethig.springbootblueprint.webapp.util.MessageKeyEnum;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationException;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class UserRegistrationServiceImpl
{
	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private OldUserServiceImpl userService;
	@Autowired private EmailServiceImpl emailService;
	@Autowired private UserTokenServiceImpl userTokenService;

	@Autowired private PasswordEncoder passwordEncoder;

	@Autowired private UserRepository userRepository;

	public UserRegistrationDto create()
	{
		return new UserRegistrationDto();
	}

	public ValidationResult validate(UserRegistrationDto user)
	{
		ValidationResult result = new ValidationResult();

		if (user == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(user));

		return result;
	}

	private void checkValidation(UserRegistrationDto user)
	{
		ValidationResult result = validate(user);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUniqueness(UserRegistrationDto user)
	{
		ValidationResult result = new ValidationResult();

		User existing = userRepository.findOneByEmailIgnoreCase(user.getEmail()).orElse(null);
		if (existing != null)
			result.addError("email", MessageKeyEnum.COMMON_VALIDATION_ALREADY_EXISTS);

		return result;
	}

	public UserDto save(UserRegistrationDto userDto)
	{
		checkValidation(userDto);

		User user = new User();
		user.setEmail(userDto.getEmail());
		user.setFullName(userDto.getFullName());
		user.setHashedPassword(passwordEncoder.encode(userDto.getPassword()));
		user.setEnabled(Boolean.FALSE);
		user.setLocked(Boolean.FALSE);
		user.setFailedLoginAttempts(0);
		user.setDeleted(Boolean.FALSE);

		return userService.transform(userRepository.save(user));
	}

	public void sendRegistrationConfirmMail(UserDto userDto, Locale locale) throws MessagingException
	{
		UserTokenDto registrationConfirmToken = userTokenService.createRegistrationConfirmToken(userDto);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(applicationProperties.getBaseUrl());
		uriBuilder.path("/auth/registration-confirm");
		uriBuilder.queryParam("token", registrationConfirmToken.getToken());

		URI registrationConfirmUri = uriBuilder.build().toUri();

		emailService.sendRegistrationConfirmMessage(userDto, locale, registrationConfirmUri);
	}

	public void resendRegistrationConfirmMail(String email, Locale locale) throws MessagingException
	{
		User user = userRepository.findOneByEmailIgnoreCase(email).orElseThrow();
		UserDto userDto = userService.transform(user);

		sendRegistrationConfirmMail(userDto, locale);
	}

	public boolean activateUser(String token)
	{
		UserTokenDto registrationConfirmToken = userTokenService.findRegistrationConfirmToken(token);
		if (registrationConfirmToken == null || registrationConfirmToken.isExpired())
			return false;

		User user = userRepository.findById(registrationConfirmToken.getUser().getId()).orElse(null);
		if (user == null)
			return false;

		user.setEnabled(true);
		userRepository.save(user);

		userTokenService.deleteRegistrationConfirmToken(registrationConfirmToken.getUser());

		return true;
	}
}
