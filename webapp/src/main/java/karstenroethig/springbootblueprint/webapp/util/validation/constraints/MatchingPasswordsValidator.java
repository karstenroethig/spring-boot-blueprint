package karstenroethig.springbootblueprint.webapp.util.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import karstenroethig.springbootblueprint.webapp.model.dto.auth.IPasswordInput;

public class MatchingPasswordsValidator implements ConstraintValidator<MatchingPasswords, IPasswordInput>
{
	private String message;

	@Override
	public void initialize(MatchingPasswords constraintAnnotation)
	{
		this.message = constraintAnnotation.message();
	}

	@Override
	public boolean isValid(IPasswordInput passwordInput, ConstraintValidatorContext context)
	{
		boolean valid = StringUtils.equals(passwordInput.getPassword(), passwordInput.getRepeatPassword());

		if (!valid)
			context.buildConstraintViolationWithTemplate(message)
				.addPropertyNode("repeatPassword")
				.addConstraintViolation()
				.disableDefaultConstraintViolation();

		return valid;
	}
}
