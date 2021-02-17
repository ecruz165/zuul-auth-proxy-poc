package com.yourproject.userinterface.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GuestDashboardController {

	@PreAuthorize("#oauth2.hasScope('openid') and hasRole('ROLE_GUEST')")
	@RequestMapping("/guest")
	public String viewGuestDashboard() {
		return "dashboardGuest.html";
	}
}
