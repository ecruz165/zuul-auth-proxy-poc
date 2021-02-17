package com.yourproject.poc.gateway;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableOAuth2Sso
@EnableResourceServer
@Order(value = 0)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
	private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    @Autowired
    private ResourceServerTokenServices resourceServerTokenServices;

	
	@Bean
	@Primary
	public OAuth2ClientContextFilter dynamicOauth2ClientContextFilter() {
		return new DynamicOauth2ClientContextFilter();
	}

	@Override
	public void configure(HttpSecurity http) throws Exception { // @formatter:off
		http
		.authorizeRequests()
			.antMatchers("/","/actuator/health","/uaa-service/**","/login","/uaa/login", "/webjars/**", "/error**","/uaa/oauth/token").permitAll()
			.antMatchers("/signin", "/forgotpassword", "/resetpassword").anonymous()
			.antMatchers("/dashboard/admin/**").hasAuthority("ROLE_ADMIN")
			.antMatchers("/dashboard/guest/**").hasAuthority("ROLE_GUEST")
			.antMatchers("/dashboard/parent/**", "/formsclerk/forms/**").hasAuthority("ROLE_PARENT")
			.antMatchers("/dashboard/schoolrep/**").hasAuthority("ROLE_SCHOOLREP")
			.antMatchers("/dashboard/staff/**").hasAuthority("ROLE_STAFF")
			.anyRequest().authenticated()
		.and()
			.csrf().requireCsrfProtectionMatcher(csrfRequestMatcher()).csrfTokenRepository(csrfTokenRepository())
		.and()
			.addFilterAfter(csrfHeaderFilter(), CsrfFilter.class)
			.addFilterAfter(oAuth2AuthenticationProcessingFilter(), AbstractPreAuthenticatedProcessingFilter.class)
			;

	} // @formatter:on

	/*dont delete this - enriches forms with csrf token */
	private static Filter csrfHeaderFilter() {
		return new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
				if (csrf != null) {
					Cookie cookie = new Cookie(CSRF_COOKIE_NAME, csrf.getToken());
					cookie.setPath("/");
					cookie.setSecure(true);
					response.addCookie(cookie);
				}
				filterChain.doFilter(request, response);
			}
		};
	}

	private OAuth2AuthenticationProcessingFilter oAuth2AuthenticationProcessingFilter() {
		OAuth2AuthenticationProcessingFilter oAuth2AuthenticationProcessingFilter = new OAuth2AuthenticationProcessingFilter();
		oAuth2AuthenticationProcessingFilter.setAuthenticationManager(oauthAuthenticationManager());
		oAuth2AuthenticationProcessingFilter.setStateless(false);
		return oAuth2AuthenticationProcessingFilter;
	}

	private AuthenticationManager oauthAuthenticationManager() {
		OAuth2AuthenticationManager oAuth2AuthenticationManager = new OAuth2AuthenticationManager();
		oAuth2AuthenticationManager.setTokenServices(tokenServices());
		oAuth2AuthenticationManager.setClientDetailsService(null);
		return oAuth2AuthenticationManager;
	}

	private RequestMatcher csrfRequestMatcher() {
		return new RequestMatcher() {
			// Always allow the HTTP GET method
			private final Pattern allowedMethods = Pattern.compile("^(GET|HEAD|OPTIONS|TRACE)$");

			// Disable CSFR protection on the following urls:
			private final AntPathRequestMatcher[] requestMatchers = { 
					new AntPathRequestMatcher("/uaa-service/**")
					, new AntPathRequestMatcher("/uaa/oauth/token")
					};

			@Override
			public boolean matches(HttpServletRequest request) {
				if (allowedMethods.matcher(request.getMethod()).matches()) {
					return false;
				}

				for (AntPathRequestMatcher matcher : requestMatchers) {
					if (matcher.matches(request)) {
						return false;
					}
				}
				return true;
			}
		};
	}

	private static CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName(CSRF_HEADER_NAME);
		return repository;
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		return defaultTokenServices;
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setSigningKey("123");
		return converter;
	}

}