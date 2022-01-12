package karstenroethig.springbootblueprint.webapp.controller;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import karstenroethig.springbootblueprint.webapp.controller.exceptions.ForbiddenException;
import karstenroethig.springbootblueprint.webapp.controller.exceptions.InternalServerErrorException;
import karstenroethig.springbootblueprint.webapp.controller.exceptions.NotFoundException;
import karstenroethig.springbootblueprint.webapp.controller.util.AttributeNames;
import karstenroethig.springbootblueprint.webapp.controller.util.UrlMappings;
import karstenroethig.springbootblueprint.webapp.controller.util.ViewEnum;
import karstenroethig.springbootblueprint.webapp.model.dto.OldUserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserRegistrationDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.VerificationTokenDto;
import karstenroethig.springbootblueprint.webapp.service.impl.OldUserServiceImpl;
import karstenroethig.springbootblueprint.webapp.service.impl.UserRegistrationServiceImpl;
import karstenroethig.springbootblueprint.webapp.util.MessageKeyEnum;
import karstenroethig.springbootblueprint.webapp.util.Messages;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_AUTH)
public class AuthController extends AbstractController
{
	@Autowired private UserRegistrationServiceImpl userRegistrationService;
	@Autowired private OldUserServiceImpl userService;

	@GetMapping(value = "/login")
	public String login(Model model, @RequestParam(name = "failed", defaultValue = "false") boolean failed, HttpServletRequest request)
	{
		if (failed)
		{
			Exception exception = (Exception)request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
			if (exception != null)
			{
				if (exception instanceof DisabledException)
					return ViewEnum.AUTH_REGISTRATION_CONFIRM_PENDING.getViewName();
				model.addAttribute("authMsg", exception.getMessage());
			}
		}

		return ViewEnum.AUTH_LOGIN.getViewName();
	}

	@GetMapping(value = "/access-denied")
	public String accessDenied(Model model) throws ForbiddenException
	{
		throw new ForbiddenException();
	}

	@GetMapping(value = "/logout-success")
	public String logoutSuccess(Model model)
	{
		return ViewEnum.AUTH_LOGOUT_SUCCESS.getViewName();
	}

	@GetMapping(value = "/registration")
	public String registration(Model model)
	{
		model.addAttribute(AttributeNames.USER, userRegistrationService.create());
		return ViewEnum.AUTH_REGISTRATION.getViewName();
	}

	@PostMapping(value = "/registration")
	public String registration(@ModelAttribute(AttributeNames.USER) @Valid UserRegistrationDto user, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model, HttpServletRequest request)
	{
		ValidationResult validationResult = userRegistrationService.validate(user);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		if (bindingResult.hasErrors() || validationResult.hasErrors())
			return ViewEnum.AUTH_REGISTRATION.getViewName();

		try
		{
			UserDto registeredUser = userRegistrationService.save(user);
			Locale currentLocale = LocaleContextHolder.getLocale();
			userRegistrationService.sendRegistrationConfirmMail(registeredUser, currentLocale);

			model.addAttribute(AttributeNames.USER, registeredUser);
			return ViewEnum.AUTH_REGISTRATION_SUCCESS.getViewName();
		}
		catch (Exception ex)
		{
			log.error("unable to register user", ex);
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.AUTH_REGISTRATION_SAVE_ERROR));
			return ViewEnum.AUTH_REGISTRATION.getViewName();
		}
	}

	@GetMapping(value = "/registration-confirm")
	public String registrationConfirm(Model model, @RequestParam(name = "token") String token)
	{
		VerificationTokenDto tokenDto = userRegistrationService.findVerifikationToken(token);
		if (tokenDto == null)
		{
			delay();
			throw new NotFoundException("token not found");
		}

		try
		{
			if (tokenDto.isExpired())
			{
				Locale currentLocale = LocaleContextHolder.getLocale();
				userRegistrationService.sendRegistrationConfirmMail(tokenDto.getUser(), currentLocale);

				return ViewEnum.AUTH_REGISTRATION_CONFIRM_EXPIRED.getViewName();
			}

			userRegistrationService.activateUser(token);
			return ViewEnum.AUTH_REGISTRATION_CONFIRM_SUCCESS.getViewName();
		}
		catch (Exception ex)
		{
			log.error("unable to confirm registration", ex);
			throw new InternalServerErrorException("error confirming user registration", ex);
		}
	}

	@GetMapping(value = UrlMappings.CONTROLLER_USER + "/show")
	public String show(Model model)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null)
			throw new NotFoundException("user not found");

		String username = authentication.getName();
		OldUserDto user = userService.find(username);
		if (user == null)
			throw new NotFoundException(username);

		for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
			user.addSessionAuthority(authority.getAuthority());

		model.addAttribute(AttributeNames.USER, user);

		return ViewEnum.USER_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.CONTROLLER_USER + "/edit")
	public String edit(Model model)
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		OldUserDto user = userService.find(username);
		if (user == null)
			throw new NotFoundException(username);

		model.addAttribute(AttributeNames.USER, user);
		return ViewEnum.USER_EDIT.getViewName();
	}

	@PostMapping(value = UrlMappings.CONTROLLER_USER + "/update")
	public String update(@ModelAttribute(AttributeNames.USER) @Valid OldUserDto user, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		OldUserDto currentUser = userService.find(username);
		if (currentUser == null)
			throw new NotFoundException(username);
		user.setId(currentUser.getId());

		if (!validate(user, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_UPDATE_INVALID));
			return ViewEnum.USER_EDIT.getViewName();
		}

		if (userService.update(user) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.USER_UPDATE_SUCCESS));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_USER, "/show");
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_UPDATE_ERROR));
		return ViewEnum.USER_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.CONTROLLER_USER + "/delete")
	public String delete(final RedirectAttributes redirectAttributes, Model model)
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		OldUserDto user = userService.find(username);
		if (user == null)
			throw new NotFoundException(username);

		if (userService.delete(user.getId()))
			return UrlMappings.redirect(UrlMappings.LOGOUT);

		redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
			Messages.createWithError(MessageKeyEnum.USER_DELETE_ERROR));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_USER, "/show");
	}

	private boolean validate(OldUserDto user, BindingResult bindingResult)
	{
		ValidationResult validationResult = userService.validate(user);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("User %s does not exist.", ex.getMessage()));
	}
}
