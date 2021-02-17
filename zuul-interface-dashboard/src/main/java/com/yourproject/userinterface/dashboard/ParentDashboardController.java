package com.yourproject.userinterface.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ParentDashboardController {

	@PreAuthorize("#oauth2.hasScope('openid') and hasRole('ROLE_PARENT')")
	@RequestMapping("/parent")
	public String viewParentDashboard() {
		return "dashboardParent.html";
	}
}