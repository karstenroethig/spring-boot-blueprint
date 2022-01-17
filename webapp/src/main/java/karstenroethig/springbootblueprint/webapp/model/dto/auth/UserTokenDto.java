package karstenroethig.springbootblueprint.webapp.model.dto.auth;

import java.time.LocalDateTime;

import karstenroethig.springbootblueprint.webapp.model.enums.UserTokenTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserTokenDto
{
	private UserTokenTypeEnum type;

	private UserDto user;

	private String token;

	private LocalDateTime expiredDatetime;

	public boolean isExpired()
	{
		LocalDateTime now = LocalDateTime.now();
		return now.isAfter(expiredDatetime);
	}
}
