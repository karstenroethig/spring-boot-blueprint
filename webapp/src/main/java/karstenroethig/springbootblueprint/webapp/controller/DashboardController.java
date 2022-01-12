package karstenroethig.springbootblueprint.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import karstenroethig.springbootblueprint.webapp.controller.util.UrlMappings;
import karstenroethig.springbootblueprint.webapp.controller.util.ViewEnum;
import karstenroethig.springbootblueprint.webapp.model.domain.User_;
import karstenroethig.springbootblueprint.webapp.model.dto.OldUserDto;
import karstenroethig.springbootblueprint.webapp.model.dto.search.UserSearchDto;
import karstenroethig.springbootblueprint.webapp.model.dto.search.UserSearchDto.NewRegisteredSearchTypeEnum;
import karstenroethig.springbootblueprint.webapp.service.impl.UserAdminServiceImpl;

@ComponentScan
@Controller
public class DashboardController
{
	@Autowired private UserAdminServiceImpl userService;

	@GetMapping(value = {UrlMappings.HOME, UrlMappings.DASHBOARD})
	public String dashborad(Model model)
	{
		addAttributesForAdminCard(model);

		return ViewEnum.DASHBOARD.getViewName();
	}

	private void addAttributesForAdminCard(Model model)
	{
		UserSearchDto userSearch = new UserSearchDto();
		userSearch.setNewRegisteredSearchType(NewRegisteredSearchTypeEnum.NEW_REGISTERED);

		Page<OldUserDto> newRegisteredUserPage = userService.findBySearchParams(userSearch, PageRequest.of(0, 1, Sort.by(User_.EMAIL)));
		long numberOfNewRegisteredUsers = newRegisteredUserPage.getTotalElements();

		boolean showAdminCard = numberOfNewRegisteredUsers > 0;

		model.addAttribute("numberOfNewRegisteredUsers", numberOfNewRegisteredUsers);
		model.addAttribute("showAdminCard", showAdminCard);
	}
}
