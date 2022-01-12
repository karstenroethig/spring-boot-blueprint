package karstenroethig.springbootblueprint.webapp.util.validation;

import karstenroethig.springbootblueprint.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class ValidationMessage
{
	private ValidationState state;
	private MessageKeyEnum key;
	private Object[] params;

	public ValidationMessage(ValidationState state, MessageKeyEnum key, Object... params)
	{
		this.state = state;
		this.key = key;
		this.params = params;
	}
}
