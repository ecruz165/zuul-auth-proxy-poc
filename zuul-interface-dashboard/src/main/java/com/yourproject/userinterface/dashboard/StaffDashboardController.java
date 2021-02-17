package com.yourproject.userinterface.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StaffDashboardController {

	@PreAuthorize("#oauth2.hasScope('openid') and hasRole('ROLE_STAFF')")
	@RequestMapping("/staff")
	public String viewStaffDashboard() {
		return "dashboardStaff.html";
	}
}
