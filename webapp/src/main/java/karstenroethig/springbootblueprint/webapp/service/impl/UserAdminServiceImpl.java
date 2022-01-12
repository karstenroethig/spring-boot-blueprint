package karstenroethig.springbootblueprint.webapp.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import karstenroethig.springbootblueprint.webapp.config.SecurityConfiguration.Authorities;
import karstenroethig.springbootblueprint.webapp.model.domain.Authority;
import karstenroethig.springbootblueprint.webapp.model.domain.User;
import karstenroethig.springbootblueprint.webapp.model.domain.User_;
import karstenroethig.springbootblueprint.webapp.model.dto.OldUserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.search.UserSearchDto;
import karstenroethig.springbootblueprint.webapp.repository.AuthorityRepository;
import karstenroethig.springbootblueprint.webapp.repository.specification.UserSpecifications;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class UserAdminServiceImpl extends OldUserServiceImpl
{
	private static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;

	private static final PageRequest ALL_ELEMENTS_PAGE_REQUEST = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(User_.EMAIL));

	private static final UserSearchDto EMPTY_SEACH_PARAMS = new UserSearchDto();

	@Autowired AuthorityRepository authorityRepository;

	public long count()
	{
		return userRepository.count();
	}

	public Page<OldUserDto> findBySearchParams(UserSearchDto userSearchDto, Pageable pageable)
	{
		Page<User> page = userRepository.findAll(UserSpecifications.matchesSearchParam(userSearchDto), pageable);
		return page.map(this::transformOld);
	}

	public Page<OldUserDto> findAll(Pageable pageable)
	{
		Page<User> page = userRepository.findAll(pageable);
		return page.map(this::transformOld);
	}

	public List<OldUserDto> findAll()
	{
		Page<OldUserDto> pageDto = findBySearchParams(EMPTY_SEACH_PARAMS, ALL_ELEMENTS_PAGE_REQUEST);
		return pageDto.getContent();
	}

	@Override
	protected User mergeSave(User user, OldUserDto userDto)
	{
		if (user == null || userDto == null )
			return null;

		merge(user, userDto);

		user.setEnabled(userDto.isEnabled());
		user.setLocked(Boolean.FALSE);
		user.setFailedLoginAttempts(0);
		user.setDeleted(Boolean.FALSE);

		mergeAuthorities(user, userDto);

		return user;
	}

	@Override
	protected User mergeUpdate(User user, OldUserDto userDto)
	{
		if (user == null || userDto == null )
			return null;

		merge(user, userDto);

		user.setEnabled(userDto.isEnabled());
		user.setLocked(userDto.isLocked());
		user.setFailedLoginAttempts(0);

		mergeAuthorities(user, userDto);

		return user;
	}

	private User mergeAuthorities(User user, OldUserDto userDto)
	{
		// delete unassigned authorities
		List<Authority> previousAssignedAuthorities = new ArrayList<>(user.getAuthorities());
		Set<String> alreadyAssignedAuthorityNames = new HashSet<>();
		for (Authority authority : previousAssignedAuthorities)
		{
			if (userDto.getAuthorities().contains(authority.getName()) && Authorities.ALL_AUTHORITIES.contains(authority.getName()))
				alreadyAssignedAuthorityNames.add(authority.getName());
			else
				user.removeAuthority(authority);
		}

		// add new assigned authorities
		for (String authorityName : userDto.getAuthorities())
		{
			if (!alreadyAssignedAuthorityNames.contains(authorityName) && Authorities.ALL_AUTHORITIES.contains(authorityName))
				user.addAuthority(findOrCreateAuthority(authorityName));
		}

		return user;
	}

	private Authority findOrCreateAuthority(String name)
	{
		Authority authority = authorityRepository.findOneByNameIgnoreCase(name).orElse(null);

		if (authority != null)
			return authority;

		authority = new Authority();
		authority.setName(name);

		return authorityRepository.save(authority);
	}

	public void incrementFailedLoginAttempts(String username)
	{
		User user = userRepository.findOneByEmailIgnoreCase(username).orElse(null);
		if (user != null)
		{
			Integer beforeFailedLoginAttempts = user.getFailedLoginAttempts();
			user.setFailedLoginAttempts(beforeFailedLoginAttempts + 1);

			if ((beforeFailedLoginAttempts + 1) >= MAX_FAILED_LOGIN_ATTEMPTS)
				user.setLocked(Boolean.TRUE);

			userRepository.save(user);
		}
	}

	public void resetFailedLoginAttempts(String username)
	{
		User user = userRepository.findOneByEmailIgnoreCase(username).orElse(null);
		if (user != null)
		{
			user.setFailedLoginAttempts(0);
			userRepository.save(user);
		}
	}

	@PostConstruct
	protected void createInitialUser()
	{
		if (userRepository.count() > 0)
			return;

		String username = "admin";

		OldUserDto user = new OldUserDto();
		user.setEmail(username);
		user.setPassword(username);
		user.setRepeatPassword(username);
		user.setFullName("Administrator");
		user.setEnabled(Boolean.TRUE);
		user.setAuthorities(Authorities.ALL_AUTHORITIES);
//		user.addAuthority(Authorities.USER);

		OldUserDto savedUser = save(user);

		if (savedUser != null)
			log.info("The initial user {} has been created", username);
		else
			log.error("An unexpected error occurred while creating the initial user {}.", username);
	}
}
