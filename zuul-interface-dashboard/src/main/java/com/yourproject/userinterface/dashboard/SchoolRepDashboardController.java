package com.yourproject.userinterface.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SchoolRepDashboardController {

	@PreAuthorize("#oauth2.hasScope('openid') and hasRole('ROLE_SCHOOL_REP')")
	@RequestMapping("/schoolrep")
	public String viewSchoolRepDashboard() {
		return "dashboardSchoolRep.html";
	}
}