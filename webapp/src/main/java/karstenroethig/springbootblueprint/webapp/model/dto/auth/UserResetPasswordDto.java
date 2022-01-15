package karstenroethig.springbootblueprint.webapp.model.dto.auth;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserResetPasswordDto
{
	@NotEmpty
	@Size(min = 1, max = 191)
	@Email
	private String email;
}
