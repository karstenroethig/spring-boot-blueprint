package karstenroethig.springbootblueprint.webapp.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true, exclude = {"email", "password", "repeatPassword", "fullName", "enabled", "locked", "deleted", "authorities", "sessionAuthorities"})
public class OldUserDto extends AbstractDtoId
{
	@NotNull
	@Size(min = 1, max = 191)
	private String email;

	@Size(max = 191)
	private String password;

	@Size(max = 191)
	private String repeatPassword;

	private String hashedPassword;

	@NotNull
	@Size(min = 1, max = 191)
	private String fullName;

	private boolean enabled = false;

	private boolean locked = false;

	private Integer failedLoginAttempts;

	private boolean deleted = false;

	private List<String> authorities = new ArrayList<>();

	private List<String> sessionAuthorities = new ArrayList<>();

	public void addAuthority(String authority)
	{
		authorities.add(authority);
	}

	public boolean hasAuthority(String authority)
	{
		return authorities.contains(authority);
	}

	public void addSessionAuthority(String sessionAuthority)
	{
		sessionAuthorities.add(sessionAuthority);
	}

	public boolean hasSessionAuthority(String authority)
	{
		return sessionAuthorities.contains(authority);
	}

	public List<String> getMissingSessionAuthorities()
	{
		return sessionAuthorities.stream()
				.filter(Predicate.not(authorities::contains))
				.collect(Collectors.toList());
	}
}
