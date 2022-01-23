package karstenroethig.springbootblueprint.webapp.controller.global;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import karstenroethig.springbootblueprint.webapp.bean.CurrentUserSessionBean;
import karstenroethig.springbootblueprint.webapp.controller.util.AttributeNames;
import karstenroethig.springbootblueprint.webapp.controller.util.TemplateDateUtils;
import karstenroethig.springbootblueprint.webapp.controller.util.TemplateTextUtils;
import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserDto;

@ControllerAdvice
public class GlobalControllerAdvice
{
	@Autowired private CurrentUserSessionBean currentUserBean;

	@ModelAttribute(AttributeNames.CURRENT_USER)
	public UserDto getCurrentUser()
	{
		return currentUserBean.getCurrentUser();
	}

	@ModelAttribute(AttributeNames.DATE_UTILS)
	public TemplateDateUtils getDateUtils()
	{
		return TemplateDateUtils.INSTANCE;
	}

	@ModelAttribute(AttributeNames.TEXT_UTILS)
	public TemplateTextUtils getTextUtils()
	{
		return TemplateTextUtils.INSTANCE;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder)
	{
		StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
		binder.registerCustomEditor(String.class, stringTrimmerEditor);
	}
}
