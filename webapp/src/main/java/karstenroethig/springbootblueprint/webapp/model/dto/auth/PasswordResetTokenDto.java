package karstenroethig.springbootblueprint.webapp.model.dto.auth;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class PasswordResetTokenDto
{
	private UserDto user;

	private String token;

	private LocalDateTime expiredDatetime;

	public boolean isExpired()
	{
		LocalDateTime now = LocalDateTime.now();
		return now.isAfter(expiredDatetime);
	}
}
