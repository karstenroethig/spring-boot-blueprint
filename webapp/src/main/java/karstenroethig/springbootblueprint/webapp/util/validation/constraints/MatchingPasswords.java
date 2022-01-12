package karstenroethig.springbootblueprint.webapp.util.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatchingPasswordsValidator.class)
@Documented
public @interface MatchingPasswords
{
	String message() default "{karstenroethig.springbootblueprint.webapp.util.validation.constraints.MatchingPasswords.message}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
