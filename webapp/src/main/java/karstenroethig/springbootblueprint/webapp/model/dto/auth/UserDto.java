package karstenroethig.springbootblueprint.webapp.model.dto.auth;

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
}
