package karstenroethig.springbootblueprint.webapp.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.springbootblueprint.webapp.model.domain.User;
import karstenroethig.springbootblueprint.webapp.model.domain.UserToken;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserTokenDto;
import karstenroethig.springbootblueprint.webapp.model.enums.UserTokenTypeEnum;
import karstenroethig.springbootblueprint.webapp.repository.UserRepository;
import karstenroethig.springbootblueprint.webapp.repository.UserTokenRepository;

@Service
@Transactional
public class UserTokenServiceImpl
{
	@Autowired private UserServiceImpl userService;

	@Autowired private UserRepository userRepository;
	@Autowired private UserTokenRepository userTokenRepository;

	private UserTokenDto createToken(UserDto userDto, UserTokenTypeEnum type)
	{
		User user = userRepository.findById(userDto.getId()).orElseThrow();
		String token = UUID.randomUUID().toString();

		UserToken userToken = new UserToken();
		userToken.setType(type);
		userToken.setUser(user);
		userToken.setToken(token);
		userToken.setExpiredDatetime(LocalDateTime.now().plusDays(1l));

		return transform(userTokenRepository.save(userToken));
	}

	public UserTokenDto createRegistrationConfirmToken(UserDto userDto)
	{
		return createToken(userDto, UserTokenTypeEnum.REGISTRATION_CONFIRM);
	}

	public UserTokenDto createChangePasswordToken(UserDto userDto)
	{
		return createToken(userDto, UserTokenTypeEnum.CHANGE_PASSWORD);
	}

	private UserTokenDto findToken(String token, UserTokenTypeEnum type)
	{
		if (token == null)
			return null;

		UserToken userToken = userTokenRepository.findOneByTokenAndType(token, type).orElse(null);
		if (userToken == null)
			return null;

		return transform(userToken);
	}

	public UserTokenDto findRegistrationConfirmToken(String token)
	{
		return findToken(token, UserTokenTypeEnum.REGISTRATION_CONFIRM);
	}

	public UserTokenDto findChangePasswordToken(String token)
	{
		return findToken(token, UserTokenTypeEnum.CHANGE_PASSWORD);
	}

	private void deleteTokens(UserDto userDto, UserTokenTypeEnum type)
	{
		User user = userRepository.findById(userDto.getId()).orElseThrow();
		userTokenRepository.deleteByUserAndType(user, type);
	}

	public void deleteRegistrationConfirmToken(UserDto userDto)
	{
		deleteTokens(userDto, UserTokenTypeEnum.REGISTRATION_CONFIRM);
	}

	public void deleteChangePasswordToken(UserDto userDto)
	{
		deleteTokens(userDto, UserTokenTypeEnum.CHANGE_PASSWORD);
	}

	private UserTokenDto transform(UserToken token)
	{
		UserTokenDto tokenDto = new UserTokenDto();
		tokenDto.setType(token.getType());
		tokenDto.setUser(userService.transform(token.getUser()));
		tokenDto.setToken(token.getToken());
		tokenDto.setExpiredDatetime(token.getExpiredDatetime());

		return tokenDto;
	}
}
