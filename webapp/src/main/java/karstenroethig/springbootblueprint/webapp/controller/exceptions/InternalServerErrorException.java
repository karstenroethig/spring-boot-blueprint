package karstenroethig.springbootblueprint.webapp.controller.exceptions;

public class InternalServerErrorException extends RuntimeException {

	public InternalServerErrorException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
