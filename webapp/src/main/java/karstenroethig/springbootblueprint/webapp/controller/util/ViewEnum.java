package karstenroethig.springbootblueprint.webapp.controller.util;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

public enum ViewEnum
{
	AUTH_LOGIN(ControllerEnum.AUTH, "/login"),
	AUTH_LOGOUT_SUCCESS(ControllerEnum.AUTH, "/logout-success"),

	AUTH_REGISTRATION(ControllerEnum.AUTH, "/registration"),
	AUTH_REGISTRATION_SUCCESS(ControllerEnum.AUTH, "/registration-success"),
	AUTH_REGISTRATION_CONFIRM_PENDING(ControllerEnum.AUTH, "/registration-confirm-pending"),
	AUTH_REGISTRATION_CONFIRM_SUCCESS(ControllerEnum.AUTH, "/registration-confirm-success"),
	AUTH_REGISTRATION_CONFIRM_EXPIRED(ControllerEnum.AUTH, "/registration-confirm-expired"),
	AUTH_RESET_PASSWORD(ControllerEnum.AUTH, "/reset-password"),
	AUTH_RESET_PASSWORD_SUCCESS(ControllerEnum.AUTH, "/reset-password-success"),
	AUTH_CHANGE_PASSWORD(ControllerEnum.AUTH, "/change-password"),
	AUTH_CHANGE_PASSWORD_SUCCESS(ControllerEnum.AUTH, "/change-password-success"),
	AUTH_CHANGE_PASSWORD_EXPIRED(ControllerEnum.AUTH, "/change-password-expired"),

	DASHBOARD("/dashboard"),

	ADMIN_SERVER_INFO(ControllerEnum.ADMIN, "/server-info" ),

	USER_REGISTER(ControllerEnum.USER, "/register"),
	USER_REGISTER_SUCCESS(ControllerEnum.USER, "/register-success"),
	USER_SHOW(ControllerEnum.USER, ActionEnum.SHOW),
	USER_EDIT(ControllerEnum.USER, ActionEnum.EDIT),
	USER_DELETE_SUCCESS(ControllerEnum.USER, "/delete-success"),

	USER_ADMIN_LIST(ControllerEnum.USER_ADMIN, ActionEnum.LIST),
	USER_ADMIN_SHOW(ControllerEnum.USER_ADMIN, ActionEnum.SHOW),
	USER_ADMIN_CREATE(ControllerEnum.USER_ADMIN, ActionEnum.CREATE),
	USER_ADMIN_EDIT(ControllerEnum.USER_ADMIN, ActionEnum.EDIT);

	private static final String VIEW_SUBDIRECTORY = "views";

	@Getter
	private String viewName = StringUtils.EMPTY;

	private enum ControllerEnum
	{
		AUTH,
		ADMIN,
		USER_ADMIN("/user-admin"),
		USER;

		private String path = null;

		private ControllerEnum() {}

		private ControllerEnum(String path)
		{
			this.path = path;
		}

		public String getPath()
		{
			return path != null ? path : ("/" + name().toLowerCase());
		}
	}

	private enum ActionEnum
	{
		CREATE,
		EDIT,
		LIST,
		SHOW;

		private ActionEnum() {}

		public String getPath()
		{
			return "/" + name().toLowerCase();
		}
	}

	private ViewEnum(ControllerEnum subController, ControllerEnum controller, ActionEnum action)
	{
		this(subController, controller, action.getPath());
	}

	private ViewEnum(ControllerEnum controller, ActionEnum action)
	{
		this(null, controller, action.getPath());
	}

	private ViewEnum(ControllerEnum controller, String path)
	{
		this(null, controller, path);
	}

	private ViewEnum(String path)
	{
		this(null, null, path);
	}

	private ViewEnum(ControllerEnum subController, ControllerEnum controller, String path)
	{
		StringBuilder newViewName = new StringBuilder(VIEW_SUBDIRECTORY);

		if (subController != null)
			newViewName.append(subController.getPath());

		if (controller != null)
			newViewName.append(controller.getPath());

		if (path != null)
			newViewName.append(path);

		viewName = StringUtils.removeStart(newViewName.toString(), "/"); // just in case if there is no view sub-directory
	}
}
