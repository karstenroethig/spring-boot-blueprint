package karstenroethig.springbootblueprint.webapp.model.dto.auth;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import karstenroethig.springbootblueprint.webapp.util.validation.constraints.MatchingPasswords;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString

@MatchingPasswords
public class UserChangePasswordDto implements IPasswordInput
{
	@NotEmpty
	@Size(min = 1, max = 191)
	private String token;

	@NotEmpty
	@Size(min = 1, max = 191)
	private String password;

	private String repeatPassword;
}
