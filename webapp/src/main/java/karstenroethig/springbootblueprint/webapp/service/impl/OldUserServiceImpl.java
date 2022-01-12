package karstenroethig.springbootblueprint.webapp.service.impl;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import karstenroethig.springbootblueprint.webapp.model.domain.Authority;
import karstenroethig.springbootblueprint.webapp.model.domain.User;
import karstenroethig.springbootblueprint.webapp.model.dto.OldUserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserDto;
import karstenroethig.springbootblueprint.webapp.repository.UserRepository;
import karstenroethig.springbootblueprint.webapp.util.MessageKeyEnum;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationException;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationResult;

@Service
@Primary
@Transactional
public class OldUserServiceImpl
{
	@Autowired private PasswordEncoder passwordEncoder;

	@Autowired protected UserRepository userRepository;

	public OldUserDto create()
	{
		return new OldUserDto();
	}

	public ValidationResult validate(OldUserDto user)
	{
		ValidationResult result = new ValidationResult();

		if (user == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(user));
		result.add(validatePassword(user));

		return result;
	}

	private void checkValidation(OldUserDto user)
	{
		ValidationResult result = validate(user);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUniqueness(OldUserDto user)
	{
		ValidationResult result = new ValidationResult();

		User existing = userRepository.findOneByEmailIgnoreCase(user.getEmail()).orElse(null);
		if (existing != null
				&& (user.getId() == null
				|| !existing.getId().equals(user.getId())))
			result.addError("username", MessageKeyEnum.USER_SAVE_ERROR_EXISTS_USERNAME);

		return result;
	}

	private ValidationResult validatePassword(OldUserDto user)
	{
		ValidationResult result = new ValidationResult();

		if (user.getId() == null && StringUtils.isBlank(user.getPassword()))
			result.addError("password", MessageKeyEnum.USER_SAVE_ERROR_PASSWORD_EMPTY);

		if (StringUtils.isNotBlank(user.getPassword()) && user.getPassword().length() < 5)
			result.addError("password", MessageKeyEnum.USER_SAVE_ERROR_PASSWORD_MIN_LENGTH);

		if (!StringUtils.equals(user.getPassword(), user.getRepeatPassword()))
			result.addError("repeatPassword", MessageKeyEnum.USER_SAVE_ERROR_REPEAT_PASSWORD_NOT_EQUAL);

		return result;
	}

	public OldUserDto save(OldUserDto userDto)
	{
		checkValidation(userDto);

		User user = new User();
		mergeSave(user, userDto);

		return transformOld(userRepository.save(user));
	}

	public OldUserDto update(OldUserDto userDto)
	{
		checkValidation(userDto);

		User user = userRepository.findById(userDto.getId()).orElse(null);
		if (user == null)
			return null;

		mergeUpdate(user, userDto);

		return transformOld(userRepository.save(user));
	}

	public boolean delete(Long id)
	{
		User user = userRepository.findById(id).orElse(null);
		if (user == null)
			return false;

		user.setEmail(null);
		user.setHashedPassword(null);
		user.setFullName(null);
		user.setEnabled(Boolean.FALSE);
		user.setDeleted(Boolean.TRUE);

		user.removeAuthoritiesFromUser();

		userRepository.save(user);

		return true;
	}

	public OldUserDto find(Long id)
	{
		return transformOld(userRepository.findById(id).orElse(null));
	}

	public OldUserDto find(String username)
	{
		return transformOld(userRepository.findOneByEmailIgnoreCase(username).orElse(null));
	}

	protected User merge(User user, OldUserDto userDto)
	{
		if (user == null || userDto == null )
			return null;

		user.setEmail(userDto.getEmail());
		user.setFullName(userDto.getFullName());

		if (StringUtils.isNotBlank(userDto.getPassword()))
			user.setHashedPassword(passwordEncoder.encode(userDto.getPassword()));

//		if (StringUtils.isNotBlank(userDto.getPassword()))
//		{
//			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//			String passwordHash = "{bcrypt}" + passwordEncoder.encode(userDto.getPassword());
//			user.setPassword(passwordHash);
//
//			System.out.println(passwordHash);
//		}

		return user;
	}

	protected User mergeSave(User user, OldUserDto userDto)
	{
		if (user == null || userDto == null )
			return null;

		merge(user, userDto);

		user.setEnabled(Boolean.FALSE);
		user.setLocked(Boolean.FALSE);
		user.setFailedLoginAttempts(0);
		user.setDeleted(Boolean.FALSE);

		return user;
	}

	protected User mergeUpdate(User user, OldUserDto userDto)
	{
		if (user == null || userDto == null )
			return null;

		merge(user, userDto);

		return user;
	}

	protected OldUserDto transformOld(User user)
	{
		if (user == null)
			return null;

		OldUserDto userDto = new OldUserDto();

		userDto.setId(user.getId());
		userDto.setEmail(user.getEmail());
		userDto.setHashedPassword(user.getHashedPassword());
		userDto.setFullName(user.getFullName());
		userDto.setEnabled(user.isEnabled());
		userDto.setLocked(user.isLocked());
		userDto.setFailedLoginAttempts(user.getFailedLoginAttempts());
		userDto.setDeleted(user.isDeleted());

		for (Authority authority : user.getAuthorities())
		{
			userDto.addAuthority(authority.getName());
		}

		return userDto;
	}

	protected UserDto transform(User user)
	{
		if (user == null)
			return null;

		UserDto userDto = new UserDto();

		userDto.setId(user.getId());
		userDto.setEmail(user.getEmail());
		userDto.setFullName(user.getFullName());

		return userDto;
	}

	protected User transform(OldUserDto userDto)
	{
		if (userDto == null || userDto.getId() == null)
			return null;

		return userRepository.findById(userDto.getId()).orElse(null);
	}
}
