package com.yourproject.poc.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.yourproject.test.integration.IntegrationTestConfig;
import com.yourproject.test.integration.IntegrationTestServerManager;
import com.yourproject.test.oauth2.helper.OAuthRequestHelper;

import io.restassured.response.Response;


@SpringJUnitConfig(  classes = { IntegrationTestConfig.class })
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application-test.properties")
@DisplayName("Microserice Integrations Tests For Zuul Proxy for Fontend/Microservice")
public class ZuulReverseProxyITTest  {
	
	private static String CLIENT_ID = "fooClient";
	private static String CLIENT_SECRET = "fooSecret";
	
	@Autowired
	IntegrationTestConfig config;
	IntegrationTestServerManager manager;
	OAuthRequestHelper helper;
	
	@BeforeAll
	void setUp() throws Exception {
		helper = new OAuthRequestHelper();
//		manager = new IntegrationTestServerManager(config);
//		manager.startServers(ZuulReverseProxyITTest.class.getSimpleName());
	}

	@AfterAll
	void tearDown() {
	//	manager.stopServers(ZuulReverseProxyITTest.class.getSimpleName());		
	}

	@Test @Disabled
	@DisplayName("ANONYMOUS VISITS Home PAGE")
	void requestHomePageTest() throws Exception {
		Response response  = helper.accessResource("http://localhost:8080/", CLIENT_ID, CLIENT_SECRET );
		assertEquals(HttpStatus.SC_OK, response.getStatusCode());
	}

	@Test @Disabled
	@DisplayName("ANONYMOUS VISITS Registration PAGE")
	void requestRegistrationParentPage() {
		Response response  = helper.accessResource("http://localhost:8080/registration/parent", CLIENT_ID, CLIENT_SECRET );
		assertEquals(HttpStatus.SC_OK, response.getStatusCode());
	}
	
	@Test @Disabled
	@DisplayName("ANONYMOUS VISITS SignIn PAGE")
	void requestAuthSignInPageTest() throws Exception {
		Response response  = helper.accessResource("http://localhost:8080/uaa/login", CLIENT_ID, CLIENT_SECRET );
		assertEquals(HttpStatus.SC_OK, response.getStatusCode());
	}

	@Test @Disabled
	@DisplayName("ANONYMOUS VISITS ForgotPassword PAGE")
	void requestAuthForgotPasswordPage() {
		Response response  = helper.accessResource("http://localhost:8080/uaa/forgotpassword", CLIENT_ID, CLIENT_SECRET );
		assertEquals(HttpStatus.SC_OK, response.getStatusCode());
	}
	
	@Test @Disabled
	@DisplayName("ANONYMOUS VISITS PROTECTED PAGE")
	void requestProtectedPage() {
		Response response  = helper.accessResource("http://localhost:8080/dashboard/parent", CLIENT_ID, CLIENT_SECRET );
		assertEquals(HttpStatus.SC_OK, response.getStatusCode());
	}
	
	@Test @Disabled
	@DisplayName("PARENT VISITS Home Page")
	void parentRequestHomePage() {

		Response accessTokenResponse = helper.obtainAccessToken("http://localhost:8080/uaa/oauth/token", "password", CLIENT_ID, CLIENT_SECRET, "parent", "password");
		String accessToken=null;
		try {
			accessToken = new JSONObject(accessTokenResponse.print()).getString("access_token");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		Response response  = helper.accessResource("http://localhost:8080/", accessToken);
		assertEquals(HttpStatus.SC_OK, response.getStatusCode());
	}
	
	
	@Test @Disabled
	@DisplayName("PARENT VISITS Parent Dashboard PAGE VIA BACKDOOR")
	void parentRequestDashboardGuestViaBackdoorPage() {

		Response accessTokenResponse = helper.obtainAccessToken("http://localhost:8081/uaa/oauth/token", "password", CLIENT_ID, CLIENT_SECRET, "parent", "password");
		String accessToken=null;
		try {
			accessToken = new JSONObject(accessTokenResponse.print()).getString("access_token");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		Response response  = helper.accessResource("http://localhost:8089/dashboard/parent", accessToken);
		assertEquals(HttpStatus.SC_OK, response.getStatusCode());
	}
	
	@Test  @Disabled
	@DisplayName("PARENT VISITS Parent Dashboard PAGE")
	void parentRequestDashboardGuestPage() {

		Response accessTokenResponse = helper.obtainAccessToken("http://localhost:8080/uaa/oauth/token", "password", CLIENT_ID, CLIENT_SECRET, "parent", "password");
		String accessToken=null;
		try {
			accessToken = new JSONObject(accessTokenResponse.print()).getString("access_token");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		Response response  = helper.accessResource("http://localhost:8080/dashboard/parent", accessToken);
		assertEquals(HttpStatus.SC_OK, response.getStatusCode());
	}

	
	@Test  @Disabled
	@DisplayName("PARENT IS REFUSED ACCESS TO ADMIN Dashboard PAGE")
	void parentIsRefusedAccessDashboardAdminPage() {

		Response accessTokenResponse = helper.obtainAccessToken("http://localhost:8080/uaa/oauth/token", "password", CLIENT_ID, CLIENT_SECRET, "parent", "password");
		String accessToken=null;
		try {
			accessToken = new JSONObject(accessTokenResponse.print()).getString("access_token");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		Response response  = helper.accessResource("http://localhost:8080/dashboard/admin", accessToken);
		assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusCode());
	}
}
