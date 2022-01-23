package karstenroethig.springbootblueprint.webapp.model.dto.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import karstenroethig.springbootblueprint.webapp.model.dto.AbstractDtoId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserDto extends AbstractDtoId
{
	private String email;
	private String fullName;
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
