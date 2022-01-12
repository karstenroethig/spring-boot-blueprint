package karstenroethig.springbootblueprint.webapp.util;

import lombok.Getter;

public enum MessageKeyEnum
{
	APPLICATION_VERSION("application.version"),
	APPLICATION_REVISION("application.revision"),
	APPLICATION_BUILD_DATE("application.buildDate"),

	COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY("common.validation.objectCannotBeEmpty"),
	COMMON_VALIDATION_ALREADY_EXISTS("common.validation.alreadyExists"),

	AUTH_REGISTRATION_SAVE_ERROR("auth.registration.save.error"),

	MAIL_REGISTRATION_CONFIM_SUBJECT("mail.registrationConfirm.subject"),

	// --------

	USER_SAVE_INVALID("user.save.invalid"),
	USER_SAVE_ERROR("user.save.error"),
	USER_SAVE_ERROR_EXISTS_USERNAME("user.save.error.exists.username"),
	USER_SAVE_ERROR_PASSWORD_EMPTY("user.save.error.passwordEmpty"),
	USER_SAVE_ERROR_PASSWORD_MIN_LENGTH("user.save.error.passwordMinLength"),
	USER_SAVE_ERROR_REPEAT_PASSWORD_NOT_EQUAL("user.save.error.repeatPasswordNotEqual"),
	USER_UPDATE_INVALID("user.update.invalid"),
	USER_UPDATE_SUCCESS("user.update.success"),
	USER_UPDATE_ERROR("user.update.error"),
	USER_DELETE_ERROR("user.delete.error"),

	USER_ADMIN_SAVE_INVALID("userAdmin.save.invalid"),
	USER_ADMIN_SAVE_SUCCESS("userAdmin.save.success"),
	USER_ADMIN_SAVE_ERROR("userAdmin.save.error"),
	USER_ADMIN_SAVE_ERROR_EXISTS_USERNAME("userAdmin.save.error.exists.username"),
	USER_ADMIN_UPDATE_INVALID("userAdmin.update.invalid"),
	USER_ADMIN_UPDATE_SUCCESS("userAdmin.update.success"),
	USER_ADMIN_UPDATE_ERROR("userAdmin.update.error"),
	USER_ADMIN_DELETE_SUCCESS("userAdmin.delete.success"),
	USER_ADMIN_DELETE_ERROR("userAdmin.delete.error");

	@Getter
	private String key;

	private MessageKeyEnum(String key)
	{
		this.key = key;
	}
}
