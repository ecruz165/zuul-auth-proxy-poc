package com.yourproject.poc.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.catalina.filters.RequestDumperFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateCustomizer;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;

/*
 * 
 * 
 * 
 * */
@SpringBootApplication
@EnableZuulProxy
public class App {

	@Bean
	UserInfoRestTemplateCustomizer userInfoRestTemplateCustomizer(LoadBalancerInterceptor loadBalancerInterceptor) {
		return template -> {
			List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
			interceptors.add(loadBalancerInterceptor);
			AccessTokenProviderChain accessTokenProviderChain = Stream.of( 
					new ResourceOwnerPasswordAccessTokenProvider(), 
					new AuthorizationCodeAccessTokenProvider(), 
					new ImplicitAccessTokenProvider(),
					new ClientCredentialsAccessTokenProvider())
					.peek(tp -> tp.setInterceptors(interceptors))
					.collect(Collectors.collectingAndThen(Collectors.toList(), AccessTokenProviderChain::new));

			template.setAccessTokenProvider(accessTokenProviderChain);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	
	@Bean
	RequestDumperFilter requestDumperFilter() {
		return new RequestDumperFilter();
	}
}