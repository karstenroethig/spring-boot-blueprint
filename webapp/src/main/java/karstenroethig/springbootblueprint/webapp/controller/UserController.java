package karstenroethig.springbootblueprint.webapp.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import karstenroethig.springbootblueprint.webapp.controller.exceptions.NotFoundException;
import karstenroethig.springbootblueprint.webapp.controller.util.AttributeNames;
import karstenroethig.springbootblueprint.webapp.controller.util.UrlMappings;
import karstenroethig.springbootblueprint.webapp.controller.util.ViewEnum;
import karstenroethig.springbootblueprint.webapp.model.dto.OldUserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserDto;
import karstenroethig.springbootblueprint.webapp.service.impl.UserServiceImpl;
import karstenroethig.springbootblueprint.webapp.util.MessageKeyEnum;
import karstenroethig.springbootblueprint.webapp.util.Messages;
import karstenroethig.springbootblueprint.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_USER)
public class UserController extends AbstractController
{
	@Autowired private UserServiceImpl userService;

	@GetMapping(value = "/show")
	public String show(Model model)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null)
			throw new NotFoundException("user not found");

		String username = authentication.getName();
		UserDto user = userService.find(username);
		if (user == null)
			throw new NotFoundException(username);

		for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
			user.addSessionAuthority(authority.getAuthority());

		model.addAttribute(AttributeNames.USER, user);

		return ViewEnum.USER_SHOW.getViewName();
	}

	// FIXME: Deprecated at this point

	@GetMapping(value = UrlMappings.CONTROLLER_USER + "/edit")
	public String edit(Model model)
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		OldUserDto user = userService.findOld(username);
		if (user == null)
			throw new NotFoundException(username);

		model.addAttribute(AttributeNames.USER, user);
		addBasicAttributes(model);
		return ViewEnum.USER_EDIT.getViewName();
	}

	@PostMapping(value = UrlMappings.CONTROLLER_USER + "/update")
	public String update(@ModelAttribute(AttributeNames.USER) @Valid OldUserDto user, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		OldUserDto currentUser = userService.findOld(username);
		if (currentUser == null)
			throw new NotFoundException(username);
		user.setId(currentUser.getId());

		if (!validate(user, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_UPDATE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.USER_EDIT.getViewName();
		}

		if (userService.update(user) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.USER_UPDATE_SUCCESS));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_USER, "/show");
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_UPDATE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.USER_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.CONTROLLER_USER + "/delete")
	public String delete(final RedirectAttributes redirectAttributes, Model model)
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		OldUserDto user = userService.findOld(username);
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

	private void addBasicAttributes(Model model)
	{
//		model.addAttribute("allContacts", contactService.findAll());
//		model.addAttribute("allDocumentBoxes", documentBoxService.findAll());
//		model.addAttribute("allDocumentTypes", documentTypeService.findAll());
//		model.addAttribute("allTags", tagService.findAll());
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("User %s does not exist.", ex.getMessage()));
	}
}
